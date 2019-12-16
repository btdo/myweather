package com.example.myweather.database

import androidx.room.Entity
import com.example.myweather.network.City
import com.example.myweather.network.Coord

@Entity(tableName = "location_table", primaryKeys = ["id"])
data class LocationEntity(
    val lat: Double,
    val lon: Double,
    val country: String,
    val id: Int,
    val city: String,
    val population: Int?
)

fun LocationEntity.asCityModel(): City {
    return City(Coord(lat, lon), country, id, city, null)
}

