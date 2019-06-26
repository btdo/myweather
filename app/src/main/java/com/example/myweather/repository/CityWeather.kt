package com.example.myweather.repository

data class CityWeather(val name: String)

data class DayWeather(val day: Double, val weatherId: Int, val max: Int, val min: Int)