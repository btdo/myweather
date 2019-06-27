package com.example.myweather.repository

import androidx.lifecycle.LiveData

interface WeatherRepositoryInterface {

    suspend fun getCityWeather(city: String) : CityWeather

}