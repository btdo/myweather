package com.example.myweather.database

import androidx.room.Entity
import com.example.myweather.network.City
import com.example.myweather.network.Coord
import org.json.JSONObject

@Entity(tableName = "location_table", primaryKeys = ["id"])
data class LocationEntity(
    val lat: Double,
    val lon: Double, val country: String,
    val id: Int,
    val city: String,
    val population: Int?
)

fun JSONObject.asCityLocationEntity(): LocationEntity {
    val lat = this.getJSONObject("coord").get("lat") as Double
    val lon = this.getJSONObject("coord").get("lon") as Double
    val name = this.getString("name")
    val country = this.getString("country")
    val id = this.getInt("id")
    return LocationEntity(lat, lon, country, id, name, null)
}

fun LocationEntity.asCityModel(): City {
    return City(Coord(lat, lon), country, id, city, null)
}

