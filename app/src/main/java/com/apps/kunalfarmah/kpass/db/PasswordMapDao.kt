package com.apps.kunalfarmah.kpass.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import java.util.concurrent.TimeUnit

@Dao()
interface PasswordMapDao {

    @Transaction
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPassword(passwordMap: PasswordMap)

    @Transaction
    @Update(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updatePassword(passwordMap: PasswordMap)

    @Query("SELECT * FROM passwordmap ORDER BY websiteName COLLATE NOCASE ASC")
    suspend fun getAllPasswords(): List<PasswordMap>

    @Query("SELECT * FROM passwordmap WHERE id = :id")
    suspend fun getPasswordById(id: String): PasswordMap

    @Query("SELECT * FROM passwordmap WHERE lastModified < :time AND isIgnored = 0 ORDER BY websiteName COLLATE NOCASE ASC")
    suspend fun getAllOldPasswords(time: Long = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(90)): List<PasswordMap>

    @Query("DELETE FROM passwordmap WHERE id = :id")
    suspend fun deletePassword(id: String)

    @Query("DELETE FROM passwordmap")
    suspend fun deleteAllPasswords()
}