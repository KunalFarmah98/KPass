package com.apps.kunalfarmah.kpass.utils

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import com.apps.kunalfarmah.kpass.R
import com.apps.kunalfarmah.kpass.db.PasswordMap
import java.io.BufferedWriter
import java.io.IOException
import java.io.OutputStreamWriter

object TextUtil {
    fun writePasswordsToFile(context: Context, passwords: List<PasswordMap>, fileUri: Uri) {
        val contentResolver: ContentResolver = context.contentResolver
        try {
            contentResolver.openOutputStream(fileUri)?.use { outputStream ->
                BufferedWriter(OutputStreamWriter(outputStream)).use { writer ->
                    for (password in passwords) {
                        writer.write(password.toString())
                        writer.newLine() // Add a newline after each password
                    }
                }
            }
            Toast.makeText(context,
                context.getString(R.string.passwords_exported_successfully), Toast.LENGTH_SHORT).show()
            Log.e("writePasswordsToFile", "Passwords written successfully to $fileUri")
        } catch (e: IOException) {
            Toast.makeText(context,
                context.getString(R.string.something_went_wrong_exporting_the_passwords_please_try_again), Toast.LENGTH_SHORT).show()
            Log.e("writePasswordsToFile", "Error writing passwords to file: ${e.message}")
            // Handle the error appropriately (e.g., show an error message to the user)
        } catch (e: SecurityException) {
            Log.e("writePasswordsToFile", "Security error writing passwords to file: ${e.message}")
        }
    }


}