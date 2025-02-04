package com.apps.kunalfarmah.kpass.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Upsert

@Dao()
interface PasswordMapDao {

    @Upsert
    suspend fun insertPassword(username: String, password: String)

    @Delete
    suspend fun deletePassword(username: String)
}