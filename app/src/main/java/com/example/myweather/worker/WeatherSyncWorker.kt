package com.example.myweather.worker

import android.content.Context
import android.location.Location
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myweather.database.AppDatabase
import com.example.myweather.repository.*
import com.example.myweather.utils.makeStatusNotification
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.runBlocking
import timber.log.Timber

/**
 * The worker that actually makes the call the the weather network and update the database with the latest data
 */
class WeatherSyncWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    companion object {
        const val MY_WEATHER_SYNC_BACKGROUND_WORK_NAME = "MY_WEATHER_SYNC_BACKGROUND_WORK_NAME"
    }

    private val weatherRepository: WeatherRepository by lazy {
        val database = AppDatabase.getInstance(applicationContext)
        WeatherRepositoryImpl(database)
    }

    private val geoLocationRepository: GeoLocationRepository by lazy {
        GeoLocationRepositoryImpl(applicationContext)
    }

    private val preferencesRepository: SharedPreferencesRepository by lazy {
        SharedPreferencesRepositoryImpl(applicationContext)
    }

    private var mLocationCallback = object : OnCompleteListener<Location> {
        override fun onComplete(task: Task<Location>) {
            if (task.isSuccessful) {
                task.result?.let {
                    runBlocking {
                        val address = geoLocationRepository.getAddress(it)
                        getWeather(address.city + "," + address.country)
                    }
                }
            } else {
                Timber.d("Cannot get user location")
            }
        }
    }

    override suspend fun doWork(): Result {
        try {

            val isTrackingLocationEnable = preferencesRepository.isLocationTrackingEnabled()
            val location = preferencesRepository.getDefaultLocation()

            if (isTrackingLocationEnable) {
                geoLocationRepository.getCurrentLocation(mLocationCallback)
            } else {
                getWeather(location)
            }
            return Result.success()
        } catch (error: Throwable) {
            Timber.e(error)
            return Result.failure()
        }
    }

    private suspend fun getWeather(location: String) {
        makeStatusNotification("Starting syncing $location", applicationContext)
        weatherRepository.getCurrentForecast(location, false)
        weatherRepository.getComingDaysForecast(location, false)
        makeStatusNotification("Done syncing $location", applicationContext)
    }
}