package com.apps.kunalfarmah.kpass

import android.content.Intent
import android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.apps.kunalfarmah.kpass.db.PasswordMap
import com.apps.kunalfarmah.kpass.security.BiometricPromptManager
import com.apps.kunalfarmah.kpass.security.CryptoManager
import com.apps.kunalfarmah.kpass.ui.components.AddPassword
import com.apps.kunalfarmah.kpass.ui.components.HomeScreen
import com.apps.kunalfarmah.kpass.ui.theme.KPassTheme
import com.apps.kunalfarmah.kpass.viewmodel.PasswordViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : AppCompatActivity() {

    private val mainViewModel: PasswordViewModel by viewModel()

    private val promptManager by lazy {
        BiometricPromptManager(this)
    }

    private fun authenticate() {
        promptManager.showBiometricPrompt(
            title = "Unlock App",
            description = "Please use your fingerprint or screen lock to unlock the app"
        )

    }

    fun export(passwords: List<PasswordMap> = listOf()){

    }


    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CryptoManager.context = this
        enableEdgeToEdge()
        setContent {
            KPassTheme {
                var fabEnabled by rememberSaveable {
                    mutableStateOf(true)
                }
                Scaffold(modifier = Modifier.fillMaxSize(),
                    topBar = {
                        TopAppBar(
                            title = { Text("KPass") },
                            navigationIcon = {
                                IconButton(onClick = { finish() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                }
                            },
                            actions = {
                                IconButton(onClick = { export() }) {
                                    Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Export")
                                }
                            },
                            colors = TopAppBarColors(
                                containerColor = androidx.compose.material3.MaterialTheme.colorScheme.primary,
                                scrolledContainerColor = androidx.compose.material3.MaterialTheme.colorScheme.secondaryContainer,
                                titleContentColor = Color.White,
                                navigationIconContentColor = Color.White,
                                actionIconContentColor = Color.White
                            )
                        )
                    },
                    floatingActionButton = { AddPassword { if (fabEnabled) mainViewModel.openAddOrEditPasswordDialog() } }
                )
                { innerPadding ->
                    val biometricResult by promptManager.promptResults.collectAsState(null)
                    val enrollLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartActivityForResult(),
                        onResult = {
                            //authenticate()
                        }
                    )
                    LaunchedEffect(true) {
                        //authenticate()
                    }
                    LaunchedEffect(biometricResult) {
                        if (biometricResult is BiometricPromptManager.BiometricResult.AuthenticationNotSet) {
                            if (Build.VERSION.SDK_INT >= 30) {
                                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                                    putExtra(
                                        Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                        BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                                    )
                                }
                                enrollLauncher.launch(enrollIntent)
                            }
                        }
                    }

                    HomeScreen(Modifier.padding(innerPadding), mainViewModel) { state ->
                        fabEnabled = state
                    }

//                    biometricResult?.let { result ->
//                        when (result) {
//                            is BiometricPromptManager.BiometricResult.AuthenticationError -> {
//                                Toast.makeText(
//                                    applicationContext,
//                                    "Authentication failed due to ${result.error}",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                            }
//
//                            BiometricPromptManager.BiometricResult.AuthenticationFailed -> {
//                                Toast.makeText(
//                                    applicationContext,
//                                    "Authentication failed, please try again",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                            }
//
//                            BiometricPromptManager.BiometricResult.AuthenticationSuccess -> {
//                                HomeScreen(Modifier.padding(innerPadding), mainViewModel)
//                            }
//
//                            BiometricPromptManager.BiometricResult.FeatureUnavailable -> {
//                                Toast.makeText(
//                                    applicationContext,
//                                    "Biometric Feature unavailable",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                                finish()
//                            }
//
//                            BiometricPromptManager.BiometricResult.HardwareUnavailable -> {
//                                Toast.makeText(
//                                    applicationContext,
//                                    "Biometric Hardware unavailable",
//                                    Toast.LENGTH_SHORT
//                                ).show()
//                                finish()
//                            }
//
//                            else -> {}
//
//                        }
//                    }
                }
            }
        }
    }
}