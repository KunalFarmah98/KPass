package com.apps.kunalfarmah.kpass.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PasswordMap(
    @PrimaryKey
    val username: String,
    val password: String
)