package com.apps.kunalfarmah.kpass.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database([PasswordMap::class], version = 1, exportSchema = true)
abstract class PasswordDb: RoomDatabase() {
    abstract fun passwordMapDao(): PasswordMapDao
}