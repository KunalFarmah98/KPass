package com.apps.kunalfarmah.kpass.di

import androidx.room.Room
import com.apps.kunalfarmah.kpass.db.PasswordDb
import com.apps.kunalfarmah.kpass.db.PasswordMapDao
import com.apps.kunalfarmah.kpass.repository.PasswordRepository
import com.apps.kunalfarmah.kpass.viewmodel.PasswordViewModel
import com.apps.kunalfarmah.kpass.worker.UpdatePasswordWorker
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module


val roomModule = module{
    single<PasswordDb>{
        Room.databaseBuilder(get(), PasswordDb::class.java, "password_db").build()
    }
    single<PasswordMapDao>{
        get<PasswordDb>().passwordMapDao()
    }
}

val repositoryModule = module {
    single { PasswordRepository(get()) }
}

val viewModelModule = module {
    viewModel { PasswordViewModel(get()) }
}

val workMangerModule = module{
    worker { UpdatePasswordWorker(get(), get(), get()) }
}