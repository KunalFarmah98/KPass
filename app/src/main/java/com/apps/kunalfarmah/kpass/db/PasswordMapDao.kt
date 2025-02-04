package com.apps.kunalfarmah.kpass.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert

@Dao()
interface PasswordMapDao {

    @Upsert
    suspend fun insertPassword(passwordMap: PasswordMap)

    @Query("SELECT * FROM passwordmap")
    suspend fun getAllPasswords(): List<PasswordMap>

    @Query("DELETE FROM passwordmap WHERE username = :username")
    suspend fun deletePassword(username: String)
}