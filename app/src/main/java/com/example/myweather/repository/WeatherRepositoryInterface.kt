package com.example.myweather.repository

interface WeatherRepositoryInterface {

    suspend fun getCurrentForecast(city: String, isForcedRefresh: Boolean)

    suspend fun getForecast(city: String, isForcedRefresh: Boolean)
}