package com.apps.kunalfarmah.kpass

import android.app.Application
import com.apps.kunalfarmah.kpass.di.repositoryModule
import com.apps.kunalfarmah.kpass.di.roomModule
import com.apps.kunalfarmah.kpass.di.viewModelModule
import com.apps.kunalfarmah.kpass.di.workMangerModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin

class App: Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidLogger()
            androidContext(this@App)
            workManagerFactory()
            modules(roomModule, repositoryModule, viewModelModule, workMangerModule)
        }

    }
}