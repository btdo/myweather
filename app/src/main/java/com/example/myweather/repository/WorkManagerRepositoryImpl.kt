package com.example.myweather.repository

import android.app.Application
import androidx.work.*
import com.example.myweather.worker.PopulateLocationDBWorker
import com.example.myweather.worker.WeatherSyncWorker
import java.util.concurrent.TimeUnit

/**
 * Schedule the weather hourly sync
 */
class WorkManagerRepositoryImpl constructor(application: Application) : WorkManagerRepository {

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

    override fun populateLocationDb() {
        val workRequest = OneTimeWorkRequestBuilder<PopulateLocationDBWorker>().build()
        workManager.enqueueUniqueWork(
            PopulateLocationDBWorker.MY_WEATHER_POPULATE_LOCATION_TABLE,
            ExistingWorkPolicy.REPLACE,
            workRequest
        )
    }
}