package com.example.myweather.repository


data class DayWeather(
    val city: String,
    val date: Int,
    val weatherId: Int,
    val minTemp: Double,
    val maxTemp: Double,
    val humidity: Int,
    val pressure: Double,
    val windSpeed: Double,
    val temp: Double
)