package com.apps.kunalfarmah.kpass.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [PasswordMap::class], version = 2, exportSchema = true,
    autoMigrations = [AutoMigration(from = 1, to = 2)]
)
abstract class PasswordDb: RoomDatabase() {
    abstract fun passwordMapDao(): PasswordMapDao
}