package com.example.myweather.network

import com.example.myweather.repository.CityWeather

data class NetworkCityWeather(
    val city: City,
    val cnt: Int,
    val cod: String,
    val list: List<DayWeather>,
    val message: Double
) {
    data class City(
        val coord: Coord,
        val country: String,
        val id: Int,
        val name: String,
        val population: Int
    ) {

        data class Coord(
            val lat: Double,
            val lon: Double
        )
    }

    data class DayWeather(
        val clouds: Int,
        val deg: Int,
        val dt: Double,
        val humidity: Double,
        val pressure: Double,
        val speed: Double,
        val temp: Temp,
        val weather: List<Weather>
    ) {
        data class Weather(
            val description: String,
            val icon: String,
            val id: Int,
            val main: String
        )

        data class Temp(
            val day: Double,
            val eve: Double,
            val max: Double,
            val min: Double,
            val morn: Double,
            val night: Double
        )
    }

}

fun NetworkCityWeather.asDomainModel(): CityWeather {
    return CityWeather(city.name)
}




