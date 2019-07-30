package com.example.myweather.repository

interface WeatherRepositoryInterface {

    suspend fun getCurrentForecast(city: String)

    suspend fun getDaysForecast(city: String)
}