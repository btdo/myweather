package com.example.myweather.repository

import android.app.Application
import androidx.work.*
import com.example.myweather.worker.WeatherSyncWorker
import java.util.concurrent.TimeUnit

class WorkManagerRepository(application: Application) : WorkManagerRepositoryInterface {

    private val workManager: WorkManager by lazy { WorkManager.getInstance(application) }

    override fun enableHourlySync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .build()

        val workRequest =
            PeriodicWorkRequestBuilder<WeatherSyncWorker>(60, TimeUnit.MINUTES)
                .setConstraints(constraints).build()

        workManager.enqueueUniquePeriodicWork(
            WeatherSyncWorker.MY_WEATHER_SYNC_BACKGROUND_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
    }

    override fun cancelHourlySync() {
        workManager.cancelUniqueWork(WeatherSyncWorker.MY_WEATHER_SYNC_BACKGROUND_WORK_NAME)
    }
}