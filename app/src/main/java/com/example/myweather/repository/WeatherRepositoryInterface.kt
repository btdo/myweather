package com.example.myweather.repository

interface WeatherRepositoryInterface {

    suspend fun getTodayWeather(city: String): DayWeather

    suspend fun getForecastWeather(city: String): List<DayWeather>

}