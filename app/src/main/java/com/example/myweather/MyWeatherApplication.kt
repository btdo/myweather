package com.example.myweather

import android.app.Application
import com.example.myweather.di.AppComponent
import com.example.myweather.di.DaggerAppComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class MyWeatherApplication : Application() {

    private val applicationScope = CoroutineScope(Dispatchers.Default)

    val appComponent: AppComponent by lazy {
        DaggerAppComponent.factory().create(this)
    }

    /**
     * onCreate is called before the first screen is shown to the user.
     *
     * Use it to setup any background tasks, running expensive setup operations in a background
     * thread to avoid delaying app start.
     */
    override fun onCreate() {
        super.onCreate()
        delayedInit()
    }

    /**
     * Setup WorkManager background job to 'fetch' new network data daily.
     */
    private fun setupRecurringWork() {
    }

    private fun delayedInit() {
        applicationScope.launch {
            Timber.plant(Timber.DebugTree())
            setupRecurringWork()
        }
    }
}