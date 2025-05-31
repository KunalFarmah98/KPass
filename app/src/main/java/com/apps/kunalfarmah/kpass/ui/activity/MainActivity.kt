package com.apps.kunalfarmah.kpass.ui.activity

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.biometrics.BiometricManager.Authenticators.DEVICE_CREDENTIAL
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.apps.kunalfarmah.kpass.R
import com.apps.kunalfarmah.kpass.constant.Constants
import com.apps.kunalfarmah.kpass.constant.Constants.UPDATE_PASSWORDS
import com.apps.kunalfarmah.kpass.constant.Constants.UPDATE_PASSWORD_WORK_NAME
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
import com.apps.kunalfarmah.kpass.worker.UpdatePasswordWorker
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private val mainViewModel: PasswordViewModel by viewModel()

    private lateinit var createFileLauncher: ActivityResultLauncher<String>

    private lateinit var workManager: WorkManager

    // global state to handle update old passwords dialog
    private var updatePasswords by mutableStateOf(false)

    private val promptManager by lazy {
        BiometricPromptManager(this)
    }

    private fun authenticate() {
        promptManager.showBiometricPrompt(
            title = getString(R.string.unlock_app),
            description = getString(R.string.please_use_your_fingerprint_or_screen_lock_to_unlock_the_app)
        )

    }

    private fun export(){
        createFile()
    }

    private fun createFile() {
        runOnUiThread {
            Toast.makeText(this,
                getString(R.string.please_select_the_location_for_the_export), Toast.LENGTH_SHORT)
                .show()
        }
        createFileLauncher.launch("K_Pass_Backup.pdf")
    }

    val permissionLauncher = registerForActivityResult(
    ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d("MainActivity", "Notification Permission GRANTED")
        } else {
            Log.w("MainActivity", "Notification Permission DENIED")
            Toast.makeText(this,
                getString(R.string.notification_permission_is_required_to), Toast.LENGTH_LONG).show()
        }
    }

    private fun checkAndRequestNotificationPermission(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED) {
                if(shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)){
                    AlertDialog.Builder(context)
                        .setTitle(getString(R.string.notification_permission_required))
                        .setMessage(getString(R.string.notification_permission_dialog_content))
                        .setPositiveButton(getString(R.string.grant_permission)) { dialog, _ ->
                                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        .setNegativeButton(getString(R.string.no_thanks)) { dialog, _ ->
                            dialog.cancel()
                        }
                        .create()
                        .show()
                }
                else{
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                Log.d("MainActivity", "Notification permission already granted.")
            }
        } else {
            Log.d("MainActivity", "Notification permission not required before Android 13.")
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        PreferencesManager.context  = this
        workManager = WorkManager.getInstance(this)

        // Process the initial intent that created the activity
        handleIntent(intent)

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
                var showOldPasswords by rememberSaveable {
                    mutableStateOf(false)
                }
                var showUpdatePasswordDialog by remember {
                    mutableStateOf(updatePasswords)
                }
                val isDialogOpen by remember {
                    derivedStateOf {
                        enterPassword || changePassword || deleteAllPasswords || showUpdatePasswordDialog
                    }
                }
                var allowEnterPassword by remember {
                    mutableStateOf(!isDialogOpen && !showUpdatePasswordDialog)
                }

                LaunchedEffect(updatePasswords) {
                    showUpdatePasswordDialog = updatePasswords
                }

                LaunchedEffect(true) {
                    // Get current work info for this unique work
                    val workInfoList =
                        workManager.getWorkInfosForUniqueWork(UPDATE_PASSWORD_WORK_NAME).get()
                    val isAlreadyScheduledAndActive = workInfoList.any {
                        it.state == WorkInfo.State.ENQUEUED || it.state == WorkInfo.State.RUNNING
                    }
                    if (!isAlreadyScheduledAndActive) {
                        workManager.cancelAllWork()
                        Log.d(
                            "WorkManager",
                            "Enqueuing new periodic work: $UPDATE_PASSWORD_WORK_NAME"
                        )
                        val periodicWorkRequest = PeriodicWorkRequest.Builder(
                            UpdatePasswordWorker::class.java, 7, TimeUnit.DAYS
                        )
                            .setInitialDelay(
                                2,
                                TimeUnit.HOURS
                            )
                            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.MINUTES)
                            .setConstraints(
                                Constraints.Builder()
                                    .setRequiresBatteryNotLow(true)
                                    .build()
                            )
                            .build()

                        workManager.enqueueUniquePeriodicWork(
                            UPDATE_PASSWORD_WORK_NAME,
                            ExistingPeriodicWorkPolicy.UPDATE,
                            periodicWorkRequest
                        )
                    } else {
                        Log.d(
                            "WorkManager",
                            "Work $UPDATE_PASSWORD_WORK_NAME is already scheduled: $workInfoList"
                        )
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
                                            stringResource(R.string.delete_all),
                                            stringResource(R.string.change_export_password)
                                        )
                                    ) {
                                        when (it) {
                                            getString(R.string.delete_all) -> {
                                                deleteAllPasswords = true
                                            }
                                            getString(R.string.change_export_password) -> {
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
                    floatingActionButton = { if(allowEnterPassword) AddPassword { mainViewModel.openAddOrEditPasswordDialog() } }
                )
                { innerPadding ->
                    var biometricResult by remember {
                        mutableStateOf<BiometricPromptManager.BiometricResult?>(
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)
                            null
                        else
                            BiometricPromptManager.BiometricResult.AuthenticationSuccess
                        )
                    }
                    val enrollLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.StartActivityForResult(),
                        onResult = {
                            authenticate()
                        }
                    )
                    LaunchedEffect(true) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            lifecycleScope.launch {
                                repeatOnLifecycle(Lifecycle.State.STARTED){
                                    authenticate()
                                }
                            }
                        }
                        mainViewModel.getMasterPassword {
                            CryptoManager.password = it
                        }
                        promptManager.promptResults.collectLatest {
                            biometricResult = it
                        }
                    }
                    LaunchedEffect(biometricResult) {
                        if (biometricResult is BiometricPromptManager.BiometricResult.AuthenticationNotSet) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                val enrollIntent = Intent(Settings.ACTION_BIOMETRIC_ENROLL).apply {
                                    putExtra(
                                        Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                        BIOMETRIC_STRONG or DEVICE_CREDENTIAL
                                    )
                                }
                                Toast.makeText(this@MainActivity,
                                    getString(R.string.please_set_up_your_screen_lock_to_use_this_app), Toast.LENGTH_SHORT).show()
                                enrollLauncher.launch(enrollIntent)
                            }
                        }
                    }

                    biometricResult?.let { result ->
                        when (result) {
                            is BiometricPromptManager.BiometricResult.AuthenticationError -> {
                                Log.e("AuthenticationError", result.error)
                                finish()
                            }

                            BiometricPromptManager.BiometricResult.AuthenticationFailed -> {
                                Toast.makeText(
                                    applicationContext,
                                    stringResource(R.string.authentication_failed_please_try_again),
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }

                            BiometricPromptManager.BiometricResult.AuthenticationSuccess -> {
                                checkAndRequestNotificationPermission(this)
                                HomeScreen(Modifier
                                    .padding(innerPadding)
                                    .blur(if (isDialogOpen) 2.dp else 0.dp),
                                    mainViewModel,
                                    showOldPasswords,
                                    showUpdatePasswordDialog
                                ){ enabled ->
                                    allowEnterPassword = enabled
                                }
                                if(showUpdatePasswordDialog == true){
                                    allowEnterPassword = false
                                    ConfirmationDialog(
                                        title = stringResource(R.string.password_age_alert_title),
                                        body = buildString {
                                            append(
                                                stringResource(
                                                    R.string.update_pass_dialog_body_1,
                                                    mainViewModel.oldPasswords.value.let{
                                                        if(it is DataModel.Success) it.data.size
                                                        else intent.extras?.getInt(Constants.OLD_PASSWORDS_COUNT,0) ?: 0
                                                    }
                                                ))
                                            append(stringResource(R.string.update_password_dialog_body_2))
                                            append(stringResource(R.string.update_pass_dialog_body_3))
                                        },
                                        onNegativeClick = {
                                            showUpdatePasswordDialog = false
                                            showOldPasswords = false
                                            allowEnterPassword = true
                                        },
                                        onPositiveClick = {
                                            showUpdatePasswordDialog = false
                                            showOldPasswords = true
                                            allowEnterPassword = true
                                        }
                                    )
                                }
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
                                        title = stringResource(R.string.delete_all_passwords),
                                        body = stringResource(R.string.are_you_sure_you_want_to_delete_all_passwords_they_can_not_be_recovered_again),
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
                                    stringResource(R.string.biometric_feature_unavailable),
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }

                            BiometricPromptManager.BiometricResult.HardwareUnavailable -> {
                                Toast.makeText(
                                    applicationContext,
                                    stringResource(R.string.biometric_hardware_unavailable),
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    /**
     * Helper function to process extras from an Intent and update Activity state.
     */
    private fun handleIntent(intentToProcess: Intent?) {
        intentToProcess?.extras?.getBoolean(UPDATE_PASSWORDS, false)?.let { shouldUpdate ->
            this.updatePasswords = shouldUpdate
            if (shouldUpdate) {
                mainViewModel.getAllOldPasswords()
            }
        }
    }
}