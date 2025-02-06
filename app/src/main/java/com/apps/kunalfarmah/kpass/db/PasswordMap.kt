package com.apps.kunalfarmah.kpass.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PasswordMap(
    @PrimaryKey(autoGenerate = false)
    val id: String = "",
    val websiteName: String = "",
    val websiteUrl: String?="",
    val username: String = "",
    val password: String = "",
    val lastModified: Long = System.currentTimeMillis()
)