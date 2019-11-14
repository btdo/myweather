package com.example.myweather.repository

import androidx.lifecycle.LiveData
import com.example.myweather.network.City

interface WeatherRepository {

    val todayForecast: LiveData<ForecastItem>

    val forecast: LiveData<List<ForecastItem>>

    suspend fun clearCache()

    suspend fun getCurrentForecast(location: String, isForcedRefresh: Boolean): ForecastItem

    suspend fun getComingDaysForecast(
        location: String,
        isForcedRefresh: Boolean
    ): List<ForecastItem>

    suspend fun getLocation(cityName: String): List<City>
}