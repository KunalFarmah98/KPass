package com.apps.kunalfarmah.kpass.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update

@Dao()
interface PasswordMapDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPassword(passwordMap: PasswordMap)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updatePassword(passwordMap: PasswordMap)

    @Query("SELECT * FROM passwordmap ORDER BY websiteName ASC")
    suspend fun getAllPasswords(): List<PasswordMap>

    @Query("DELETE FROM passwordmap WHERE id = :id")
    suspend fun deletePassword(id: String)

    @Query("DELETE FROM passwordmap")
    suspend fun deleteAllPasswords()
}