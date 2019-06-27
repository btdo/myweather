package com.example.myweather.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myweather.network.WeatherApi
import com.example.myweather.network.asDomainModel

class WeatherRepository : WeatherRepositoryInterface {

    override suspend fun getCityWeather(city: String) : CityWeather {
        val cityWeather=  WeatherApi.weatherService.getWeatherForCity(city).await()
        return cityWeather.asDomainModel()
    }
}