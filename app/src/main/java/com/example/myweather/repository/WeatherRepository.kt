package com.example.myweather.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myweather.database.ForecastItemDatabase
import com.example.myweather.database.asDomainModel
import com.example.myweather.network.WeatherApi
import com.example.myweather.network.asDatabaseModel
import com.example.myweather.network.asDomainModel
import com.example.myweather.utils.SunshineDateUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherRepository(private val database: ForecastItemDatabase) : WeatherRepositoryInterface {
    companion object {
        const val MIN_ITEM_FORCAST_ITEMS = 40
    }

    val todayForecast: LiveData<ForecastItem>
        get() {
            return _todayForecast
        }

    private val _todayForecast = MutableLiveData<ForecastItem>()

    val dailyForecast: LiveData<List<ForecastItem>>
        get() {
            return _dailyForecast
        }

    private val _dailyForecast = MutableLiveData<List<ForecastItem>>()


    override suspend fun getCurrentForecast(city: String) {
        withContext(Dispatchers.IO) {
            val currentDateTime = SunshineDateUtils.normalizedUtcDateForTodayHours
            var dbItem = database.forecastItemDao.query(currentDateTime, city.trim().toLowerCase())
            if (dbItem == null) {
                val networkItem = WeatherApi.weatherService.getCurrentWeather(city).await()
                dbItem = networkItem.asDatabaseModel()
                database.forecastItemDao.insert(dbItem)
            }

            _todayForecast.postValue(dbItem.asDomainModel())
        }
    }

    override suspend fun getDaysForecast(city: String) {
        withContext(Dispatchers.IO) {
            val currentDateTime = SunshineDateUtils.normalizedUtcDateForTodayHours
            val forecastItems: List<ForecastItem>
            val forecastItemsDb =
                database.forecastItemDao.queryFutureWeatherItems(currentDateTime, city)
            if (forecastItemsDb.size < MIN_ITEM_FORCAST_ITEMS) {
                val forecastWeather = WeatherApi.weatherService.getDailyForecast(city).await()
                forecastItems = forecastWeather.asDomainModel().filter { item -> item.date > currentDateTime }
                val dbItems = forecastWeather.asDatabaseModel().filter { item -> item.date > currentDateTime }
                database.forecastItemDao.insertAll(dbItems)
            } else {
                forecastItems = forecastItemsDb.map { item ->
                    item.asDomainModel()
                }
            }

            _dailyForecast.postValue(forecastItems)
        }

    }

}