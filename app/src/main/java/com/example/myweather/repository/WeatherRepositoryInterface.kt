package com.example.myweather.repository

import androidx.lifecycle.LiveData

interface WeatherRepositoryInterface {

    val todayForecast: LiveData<ForecastItem>

    val forecast: LiveData<List<ForecastItem>>

    suspend fun clearCache()

    suspend fun getCurrentForecast(city: String, isForcedRefresh: Boolean): ForecastItem

    suspend fun getComingDaysForecast(city: String, isForcedRefresh: Boolean): List<ForecastItem>
}