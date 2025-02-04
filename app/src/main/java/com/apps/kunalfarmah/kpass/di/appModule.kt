package com.apps.kunalfarmah.kpass.di

import androidx.room.Room
import com.apps.kunalfarmah.kpass.db.PasswordDb
import org.koin.dsl.module


val roomModule = module{
    single<PasswordDb>{
        Room.databaseBuilder(get(), PasswordDb::class.java, "password_db").build()
    }
}