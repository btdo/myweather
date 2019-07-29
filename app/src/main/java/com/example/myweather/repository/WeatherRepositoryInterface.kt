package com.example.myweather.repository

interface WeatherRepositoryInterface {

    suspend fun getTodayForecast(city: String)

    suspend fun getDaysForecast(city: String)
}