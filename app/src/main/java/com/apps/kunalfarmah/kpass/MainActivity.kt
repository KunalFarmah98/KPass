package com.apps.kunalfarmah.kpass

import android.content.Intent
import android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.apps.kunalfarmah.kpass.model.DataModel
import com.apps.kunalfarmah.kpass.security.BiometricPromptManager
import com.apps.kunalfarmah.kpass.security.CryptoManager
import com.apps.kunalfarmah.kpass.ui.components.AddPassword
import com.apps.kunalfarmah.kpass.ui.components.ConfirmationDialog
import com.apps.kunalfarmah.kpass.ui.components.EnterPassword
import com.apps.kunalfarmah.kpass.ui.components.HomeScreen
import com.apps.kunalfarmah.kpass.ui.components.OptionsMenu
import com.apps.kunalfarmah.kpass.ui.theme.KPassTheme
import com.apps.kunalfarmah.kpass.utils.PdfUtil
import com.apps.kunalfarmah.kpass.utils.PreferencesManager
import com.apps.kunalfarmah.kpass.viewmodel.PasswordViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel


class MainActivity : AppCompatActivity() {

    private val mainViewModel: PasswordViewModel by viewModel()

    private lateinit var createFileLauncher: ActivityResultLauncher<String>


    private val promptManager by lazy {
        BiometricPromptManager(this)
    }

    private fun authenticate() {
        promptManager.showBiometricPrompt(
            title = "Unlock App",
            description = "Please use your fingerprint or screen lock to unlock the app"
        )

    }

    private fun export(){
        createFile()
    }

    private fun createFile() {
        runOnUiThread {
            Toast.makeText(this, "Please select where to export the data", Toast.LENGTH_SHORT)
                .show()
        }
        createFileLauncher.launch("K_Pass_Backup.pdf")
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        PreferencesManager.context  = this
        createFileLauncher = registerForActivityResult(
            ActivityResultContracts.CreateDocument("application/pdf")
        ) { uri: Uri? ->
            uri?.let {
                PdfUtil.exportPasswordsToPdf(
                    this@MainActivity,
                    (mainViewModel.passwords.value as DataModel.Success).data,
                    uri,
                    CryptoManager.password
                )
            }
        }
        enableEdgeToEdge()
        setContent {
            KPassTheme {
                var enterPassword by rememberSaveable {
                    mutableStateOf(false)
                }
                var changePassword by rememberSaveable {
                    mutableStateOf(false)
                }
                var deleteAllPasswords by rememberSaveable {
                    mutableStateOf(false)
                }
                val isDialogOpen by remember {
                    derivedStateOf {
                        enterPassword || changePassword || deleteAllPasswords
                    }
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
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.End) {
                                    IconButton(onClick = {
                                        mainViewModel.getMasterPassword { password ->
                                            if(password.isEmpty()){
                                                enterPassword = true
                                                changePassword = false
                                            }
                                            else {
                                                if(!enterPassword && !changePassword) {
                                                    export()
                                                }
                                                if(changePassword){
                                                    runOnUiThread {
                                                        Toast.makeText(
                                                            this@MainActivity,
                                                            getString(R.string.password_changed_successfully),
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                }
                                                enterPassword = false
                                                changePassword = false
                                            }
                                        }
                                    }) {
                                        Image(
                                            painter = painterResource(R.drawable.baseline_save_24),
                                            contentDescription = "Export passwords",
                                            colorFilter = ColorFilter.tint(
                                                Color.White
                                            )
                                        )
                                    }
                                    OptionsMenu(
                                        titles = listOf(
                                            "Delete All",
                                            "Change export password"
                                        )
                                    ) {
                                        when (it) {
                                            0 -> {
                                                deleteAllPasswords = true
                                            }
                                            1 -> {
                                                changePassword = true
                                                enterPassword = false
                                            }
                                        }
                                    }
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
                    floatingActionButton = { AddPassword { mainViewModel.openAddOrEditPasswordDialog() } }
                )
                { innerPadding ->
                    val biometricResult by promptManager.promptResults.collectAsState(null)
                    val enrollLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartActivityForResult(),
                        onResult = {
                            authenticate()
                        }
                    )
                    LaunchedEffect(true) {
                        authenticate()
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

                    biometricResult?.let { result ->
                        when (result) {
                            is BiometricPromptManager.BiometricResult.AuthenticationError -> {
                                Toast.makeText(
                                    applicationContext,
                                    "Authentication failed due to ${result.error}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            BiometricPromptManager.BiometricResult.AuthenticationFailed -> {
                                Toast.makeText(
                                    applicationContext,
                                    "Authentication failed, please try again",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            BiometricPromptManager.BiometricResult.AuthenticationSuccess -> {
                                HomeScreen(Modifier
                                    .padding(innerPadding)
                                    .blur(if (isDialogOpen) 2.dp else 0.dp), mainViewModel)
                                if(enterPassword || changePassword){
                                    EnterPassword(onClose = {
                                        enterPassword = false
                                        changePassword = false
                                    }) { password ->
                                        if(password.isEmpty()){
                                            runOnUiThread {
                                                Toast.makeText(
                                                    this,
                                                    getString(R.string.password_can_not_be_empty),
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                            return@EnterPassword
                                        }
                                        CryptoManager.password = password
                                        mainViewModel.savePassword(password)
                                        {
                                            if(enterPassword){
                                                export()
                                            }
                                        }
                                    }
                                }
                                if(deleteAllPasswords){
                                    ConfirmationDialog(
                                        title = "Delete All Passwords",
                                        body = "Are you sure you want to delete all passwords?\nThey can not be recovered again.",
                                        onNegativeClick = {
                                            deleteAllPasswords = false

                                        },
                                        onPositiveClick = {
                                            mainViewModel.deleteAllPasswords()
                                            deleteAllPasswords = false
                                        }
                                    )
                                }
                            }

                            BiometricPromptManager.BiometricResult.FeatureUnavailable -> {
                                Toast.makeText(
                                    applicationContext,
                                    "Biometric Feature unavailable",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }

                            BiometricPromptManager.BiometricResult.HardwareUnavailable -> {
                                Toast.makeText(
                                    applicationContext,
                                    "Biometric Hardware unavailable",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }

                            else -> {}

                        }
                    }
                }
            }
        }
    }
}