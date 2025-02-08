package com.apps.kunalfarmah.kpass.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.apps.kunalfarmah.kpass.security.CryptoManager

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
        return "Website: $websiteName ($websiteUrl) | Username: $username | Password: ${CryptoManager.decrypt(password)}"

    }
}