package com.apps.kunalfarmah.kpass.db

import android.annotation.SuppressLint
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.apps.kunalfarmah.kpass.security.CryptoManager
import java.text.SimpleDateFormat
import java.util.Date

@Entity
data class PasswordMap(
    @PrimaryKey(autoGenerate = false)
    val id: String = "",
    val websiteName: String = "",
    val websiteUrl: String?="",
    val username: String = "",
    val password: String = "",
    val lastModified: Long = System.currentTimeMillis()
){
    override fun toString(): String {
        return "Website: $websiteName ($websiteUrl) | Username: $username | Password: $password"
    }

    fun toDecryptedString(): String{
        return "$websiteName ($websiteUrl)\nusername: $username\nPassword: ${CryptoManager.decrypt(password)}"
    }

    @SuppressLint("SimpleDateFormat")
    fun getDate(): String{
        val date = Date(lastModified)
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm")
        return format.format(date)
    }
}