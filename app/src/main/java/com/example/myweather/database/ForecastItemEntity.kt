package com.example.myweather.database

import androidx.room.Entity
import com.example.myweather.repository.ForecastItem

@Entity(tableName = "forecast_item_table", primaryKeys = ["date", "location"])
data class ForecastItemEntity(
    val location: String,
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
)


fun ForecastItemEntity.asDomainModel(): ForecastItem {
    return ForecastItem(
        location,
        date,
        weatherId,
        minTemp,
        maxTemp,
        humidity,
        pressure,
        windSpeed,
        degrees,
        temp,
        mainDescription,
        description
    )
}