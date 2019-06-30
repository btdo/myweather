package com.example.myweather.repository

import com.example.myweather.network.WeatherApi
import com.example.myweather.network.asDomainModel

class WeatherRepository : WeatherRepositoryInterface {

    override suspend fun getTodayWeather(city: String): DayWeather {
        val cityWeather = WeatherApi.weatherService.getTodayWeather(city).await()
        return cityWeather.asDomainModel()
    }


    override suspend fun getForecastWeather(city: String): List<DayWeather> {
        val forecastWeather = WeatherApi.weatherService.getForecastWeather(city).await()
        return forecastWeather.asDomainModel()
    }
}