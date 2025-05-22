package com.apps.kunalfarmah.kpass.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.apps.kunalfarmah.kpass.constant.Constants
import com.apps.kunalfarmah.kpass.model.DataModel
import com.apps.kunalfarmah.kpass.ui.activity.MainActivity
import com.apps.kunalfarmah.kpass.viewmodel.PasswordViewModel
import kotlinx.coroutines.flow.collectLatest

class UpdatePasswordWorker(val context: Context, workerParams: WorkerParameters, private val viewModel: PasswordViewModel) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        viewModel.getAllOldPasswords()
        viewModel.oldPasswords.collectLatest {
            if(it is DataModel.Success && it.data.isNotEmpty()){
                val channel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel("password_update", "Password Update", NotificationManager.IMPORTANCE_DEFAULT)
                } else {
                    null
                }
                val intent = Intent(context, MainActivity::class.java)
                intent.putExtra(Constants.UPDATE_PASSWORDS, true)
                intent.putExtra(Constants.OLD_PASSWORDS_COUNT, it.data.size)
                val notification = NotificationCompat.Builder(context, "password_update")
                    .setContentTitle("Update Older Passwords")
                    .setContentText("You have ${it.data.size} passwords older than 3 Months. It is recommended to update these passwords")
                    .setContentIntent(PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_IMMUTABLE))
                    .build()
                val notificationService = context.getSystemService(NotificationManager::class.java) as NotificationManager
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    notificationService.createNotificationChannel(channel!!)
                }
                if(ContextCompat.checkSelfPermission(context, android.Manifest.permission.POST_NOTIFICATIONS) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                    notificationService.notify(1, notification)
                }
            }
        }
        return Result.success()
    }
}