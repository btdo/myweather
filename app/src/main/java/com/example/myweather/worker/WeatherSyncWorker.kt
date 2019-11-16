package com.example.myweather.worker

import android.content.Context
import android.location.Location
import androidx.preference.PreferenceManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myweather.R
import com.example.myweather.database.AppDatabase
import com.example.myweather.repository.GeoLocationRepository
import com.example.myweather.repository.GeoLocationRepositoryImpl
import com.example.myweather.repository.WeatherRepository
import com.example.myweather.repository.WeatherRepositoryImpl
import com.example.myweather.utils.makeStatusNotification
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.runBlocking
import timber.log.Timber

/**
 * The worker that actually make the call the the weather network and update the database with the latest data
 */
class WeatherSyncWorker(ctx: Context, params: WorkerParameters) : CoroutineWorker(ctx, params) {

    companion object {
        const val MY_WEATHER_SYNC_BACKGROUND_WORK_NAME = "MY_WEATHER_SYNC_BACKGROUND_WORK_NAME"
    }

    private val database: AppDatabase  by lazy {
        AppDatabase.getInstance(applicationContext)
    }

    private val weatherRepository: WeatherRepository by lazy {
        WeatherRepositoryImpl(database)
    }

    private val geoLocationRepository: GeoLocationRepository by lazy {
        GeoLocationRepositoryImpl(applicationContext)
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
            val sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(applicationContext/* Activity context */)
            val isTrackingLocationEnable =
                sharedPreferences.getBoolean(
                    applicationContext.resources.getString(R.string.pref_enable_geo_location_key),
                    false
                )
            val location = sharedPreferences.getString(
                applicationContext.resources.getString(R.string.pref_location_key),
                applicationContext.resources.getString(R.string.pref_location_default)
            )!!

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