package com.example.myweather.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myweather.database.AppDatabase
import com.example.myweather.database.asCityModel
import com.example.myweather.database.asDomainModel
import com.example.myweather.network.Location
import com.example.myweather.network.WeatherApi
import com.example.myweather.network.asDatabaseModel
import com.example.myweather.network.asDomainModel
import com.example.myweather.utils.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherRepositoryImpl constructor(private val database: AppDatabase) : WeatherRepository {
    companion object {
        const val MIN_ITEM_FORCAST_ITEMS = 40
    }

    override val todayForecast: LiveData<ForecastItem>
        get() {
            return _currentForecast
        }

    private val _currentForecast = MutableLiveData<ForecastItem>()

    override val forecast: LiveData<List<ForecastItem>>
        get() {
            return _forecast
        }

    private val _forecast = MutableLiveData<List<ForecastItem>>()

    override suspend fun clearCache() {
        withContext(Dispatchers.IO) {
            database.forecastItemDao.clearAll()
        }
    }

    override suspend fun getLocation(cityName: String): List<Location> {
        return withContext(Dispatchers.IO) {
            val dbCities = database.locationDao.queryCity(cityName)
            var modelCities = listOf<Location>()
            dbCities?.let {
                modelCities = it.map { locationEntity ->
                    locationEntity.asCityModel()
                }
            }

            return@withContext modelCities
        }
    }

    override suspend fun getCurrentForecast(
        location: String,
        isForcedRefresh: Boolean
    ): ForecastItem {
        return withContext(Dispatchers.IO) {
            val currentHour = DateUtils.getCurrentHour()
            var dbItem = database.forecastItemDao.query(currentHour, location.trim().toLowerCase())
            if (dbItem == null || isForcedRefresh) {
                val networkItem = WeatherApi.weatherService.getCurrentWeather(location).await()
                dbItem = networkItem.asDatabaseModel()
                database.forecastItemDao.insert(dbItem)
                database.forecastItemDao.clearPastItems(location, currentHour)
            }

            val domainModel = dbItem.asDomainModel()
            _currentForecast.postValue(domainModel)
            return@withContext domainModel
        }
    }

    /**
     * backend return forecast for the next 5 days with a 3 hour interval
     */
    override suspend fun getComingDaysForecast(
        location: String,
        isForcedRefresh: Boolean
    ): List<ForecastItem> {
        return withContext(Dispatchers.IO) {
            val currentHour = DateUtils.getCurrentHour()
            val forecastItems: List<ForecastItem>
            val forecastItemsDb =
                database.forecastItemDao.queryFutureWeatherItems(
                    currentHour,
                    location.trim().toLowerCase()
                )
            if (forecastItemsDb.size < MIN_ITEM_FORCAST_ITEMS || isForcedRefresh) {
                val forecastWeather = WeatherApi.weatherService.getDailyForecast(location).await()
                forecastItems =
                    forecastWeather.asDomainModel().filter { item -> item.date > currentHour }
                val dbItems =
                    forecastWeather.asDatabaseModel().filter { item -> item.date > currentHour }
                database.forecastItemDao.clearFutureItems(location, currentHour)
                database.forecastItemDao.insertAll(dbItems)
            } else {
                forecastItems = forecastItemsDb.map { item ->
                    item.asDomainModel()
                }
            }

            _forecast.postValue(forecastItems)
            return@withContext forecastItems
        }
    }

}