package com.apps.kunalfarmah.kpass.security

import android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class BiometricPromptManager(
    private val activity: AppCompatActivity
) {
    private val results = MutableSharedFlow<BiometricResult>(replay = 1, extraBufferCapacity = 1)
    val promptResults = results.asSharedFlow()

    fun showBiometricPrompt(
        title: String,
        description: String
    ) {
        val manager = BiometricManager.from(activity)
        val authenticators = if(Build.VERSION.SDK_INT >= 30) {
            BIOMETRIC_STRONG or DEVICE_CREDENTIAL
        } else BIOMETRIC_STRONG

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setDescription(description)
            .setAllowedAuthenticators(authenticators)
            .setConfirmationRequired(false)

        if(Build.VERSION.SDK_INT < 30) {
            promptInfo.setNegativeButtonText("Cancel")
        }

        when(manager.canAuthenticate(authenticators)) {
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                results.tryEmit(BiometricResult.HardwareUnavailable)
                return
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                results.tryEmit(BiometricResult.FeatureUnavailable)
                return
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                results.tryEmit(BiometricResult.AuthenticationNotSet)
                return
            }
            else -> Unit
        }

        val prompt = BiometricPrompt(
            activity,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    results.tryEmit(BiometricResult.AuthenticationError(errString.toString()))
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    results.tryEmit(BiometricResult.AuthenticationSuccess)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    results.tryEmit(BiometricResult.AuthenticationFailed)
                }
            }
        )
        prompt.authenticate(promptInfo.build())
    }

    sealed interface BiometricResult {
        data object HardwareUnavailable: BiometricResult
        data object FeatureUnavailable: BiometricResult
        data class AuthenticationError(val error: String): BiometricResult
        data object AuthenticationFailed: BiometricResult
        data object AuthenticationSuccess: BiometricResult
        data object AuthenticationNotSet: BiometricResult
    }
}