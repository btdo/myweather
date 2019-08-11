package com.example.myweather.repository

interface WeatherRepositoryInterface {

    suspend fun getCurrentForecast(city: String, isForcedRefresh: Boolean)

    suspend fun getDaysForecast(city: String, isForcedRefresh: Boolean)
}