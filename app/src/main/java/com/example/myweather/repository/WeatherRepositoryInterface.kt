package com.example.myweather.repository

interface WeatherRepositoryInterface {

    suspend fun clearCache()

    suspend fun getCurrentForecast(city: String, isForcedRefresh: Boolean): ForecastItem

    suspend fun getComingDaysForecast(city: String, isForcedRefresh: Boolean): List<ForecastItem>
}