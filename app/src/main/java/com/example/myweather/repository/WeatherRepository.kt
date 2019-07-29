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

    val todayForecast: LiveData<ForecastItem>
        get() {
            return _todayForecast
        }

    private val _todayForecast = MutableLiveData<ForecastItem>()

    val dailyForecast: LiveData<ForecastItem>
        get() {
            return _dailyForecast
        }

    private val _dailyForecast = MutableLiveData<ForecastItem>()


    override suspend fun getTodayForecast(city: String) {
        withContext(Dispatchers.IO) {
            var dbItem = database.forecastItemDao.query(SunshineDateUtils.normalizedUtcDateForTodayHours, city)
            if (dbItem == null) {
                val networkItem = WeatherApi.weatherService.getCurrentWeather(city).await()
                dbItem = networkItem.asDatabaseModel()
                database.forecastItemDao.insert(dbItem)
            }
            _todayForecast.value = dbItem.asDomainModel()
        }
    }

    override suspend fun getDaysForecast(city: String) {
        withContext(Dispatchers.IO) {
            var forecastItemsDb =
                database.forecastItemDao.queryFutureWeatherItems(SunshineDateUtils.normalizedUtcDateForTodayHours, city)
            var lastItem = if (forecastItemsDb == null) null else forecastItemsDb.get(forecastItemsDb.lastIndex)
            // TODO: check that there are at least 40 items in the list ( 8 intervals of 3 hours from current time * 5 days = 40)
        }

    }

    private suspend fun getNetworkDailyForecast(city: String): List<ForecastItem> {
        val forecastWeather = WeatherApi.weatherService.getDailyForecast(city).await()
        return forecastWeather.asDomainModel()
    }

}