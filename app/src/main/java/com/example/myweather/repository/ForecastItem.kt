package com.example.myweather.repository

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ForecastItem(
    val city: String,
    val date: Long,
    val weatherId: Int,
    val minTemp: Double,
    val maxTemp: Double,
    val humidity: Int,
    val pressure: Double,
    val windSpeed: Float,
    val degrees: Float,
    val temp: Double,
    val mainDescription: String,
    val description: String
) : Parcelable

@Parcelize
data class HourForecast(
    val city: String,
    val date: Long,
    val weatherId: Int,
    val temp: Double,
    val windSpeed: Float
) : Parcelable