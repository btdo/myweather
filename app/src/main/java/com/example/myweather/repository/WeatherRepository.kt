package com.example.myweather.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myweather.database.ForecastItemDatabase
import com.example.myweather.database.asDomainModel
import com.example.myweather.network.WeatherApi
import com.example.myweather.network.asDatabaseModel
import com.example.myweather.network.asDomainModel
import com.example.myweather.utils.DateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherRepository(private val database: ForecastItemDatabase) : WeatherRepositoryInterface {
    companion object {
        const val MIN_ITEM_FORCAST_ITEMS = 40
    }

    val todayForecast: LiveData<ForecastItem>
        get() {
            return _currentForecast
        }

    private val _currentForecast = MutableLiveData<ForecastItem>()

    val forecast: LiveData<List<ForecastItem>>
        get() {
            return _forecast
        }

    private val _forecast = MutableLiveData<List<ForecastItem>>()

    override suspend fun clearCache() {
        withContext(Dispatchers.IO) {
            database.forecastItemDao.clearAll()
        }
    }

    override suspend fun getCurrentForecast(city: String, isForcedRefresh: Boolean) {
        withContext(Dispatchers.IO) {
            val currentHour = DateUtils.getCurrentHour()
            var dbItem = database.forecastItemDao.query(currentHour, city.trim().toLowerCase())
            if (dbItem == null || isForcedRefresh) {
                val networkItem = WeatherApi.weatherService.getCurrentWeather(city).await()
                dbItem = networkItem.asDatabaseModel()
                database.forecastItemDao.insert(dbItem)
                database.forecastItemDao.clearPastItems(city, currentHour)
            }
            _currentForecast.postValue(dbItem.asDomainModel())
        }
    }

    /**
     * backend return forecast for the next 5 days with a 3 hour interval
     */
    override suspend fun getComingDaysForecast(city: String, isForcedRefresh: Boolean) {
        withContext(Dispatchers.IO) {
            val currentHour = DateUtils.getCurrentHour()
            val forecastItems: List<ForecastItem>
            val forecastItemsDb =
                database.forecastItemDao.queryFutureWeatherItems(currentHour, city)
            if (forecastItemsDb.size < MIN_ITEM_FORCAST_ITEMS || isForcedRefresh) {
                val forecastWeather = WeatherApi.weatherService.getDailyForecast(city).await()
                forecastItems = forecastWeather.asDomainModel().filter { item -> item.date > currentHour }
                val dbItems = forecastWeather.asDatabaseModel().filter { item -> item.date > currentHour }
                database.forecastItemDao.clearFutureItems(city, currentHour)
                database.forecastItemDao.insertAll(dbItems)
            } else {
                forecastItems = forecastItemsDb.map { item ->
                    item.asDomainModel()
                }
            }

            _forecast.postValue(forecastItems)
        }
    }

}