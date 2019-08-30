package com.example.myweather.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myweather.database.ForecastItemDatabase
import com.example.myweather.repository.WeatherRepository
import com.example.myweather.utils.makeStatusNotification
import timber.log.Timber

const val KEY_CITY_SYNC = "KEY_CITY_SYNC"

class WeatherSyncWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        try {
            val city = inputData.getString(KEY_CITY_SYNC) ?: return Result.failure()
            makeStatusNotification("Starting syncing $city", applicationContext)
            val database = ForecastItemDatabase.getInstance(applicationContext)
            val repository = WeatherRepository(database)
            repository.getCurrentForecast(city, true)
            repository.getComingDaysForecast(city, true)
            makeStatusNotification("Done syncing $city", applicationContext)
            return Result.success()
        } catch (error: Throwable) {
            Timber.e(error)
            return Result.failure()
        }
    }
}