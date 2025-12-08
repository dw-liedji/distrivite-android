package com.datavite.distrivite.app

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import jakarta.inject.Inject

@HiltAndroidApp
class TiqtaqApp: Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory
    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    @Inject lateinit var appStartupInitializer: AppStartupInitializer
    @Inject lateinit var appLifecycleObserver: AppLifecycleObserver

    override fun onCreate() {
        super.onCreate()
        // Launch startup sync without blocking app launch
        ProcessLifecycleOwner.get().lifecycle.addObserver(appLifecycleObserver)
        appStartupInitializer.setupWork()
    }
}