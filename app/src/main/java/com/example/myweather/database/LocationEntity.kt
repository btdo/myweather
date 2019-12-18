package com.example.myweather.database

import androidx.room.Entity
import com.example.myweather.network.Coord
import com.example.myweather.network.Location

@Entity(tableName = "location_table", primaryKeys = ["id"])
data class LocationEntity(
    val lat: Double,
    val lon: Double,
    val country: String,
    val id: Int,
    val name: String,
    val population: Int?
)

fun LocationEntity.asCityModel(): Location {
    return Location(Coord(lat, lon), country, id, name, null)
}

