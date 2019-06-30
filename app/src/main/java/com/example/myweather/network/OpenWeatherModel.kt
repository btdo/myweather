package com.example.myweather.network

import com.example.myweather.repository.DayWeather
import com.squareup.moshi.Json


data class TodayOpenWeather(
    val base: String,
    val clouds: Clouds,
    val cod: Int,
    val coord: Coord,
    val dt: Int,
    val id: Int,
    val main: Main,
    val name: String,
    val sys: Sys,
    val visibility: Int,
    val weather: List<Weather>,
    val wind: Wind
)

fun TodayOpenWeather.asDomainModel(): DayWeather {
    return DayWeather(
        name,
        dt,
        weather[0].id,
        main.temp_min,
        main.temp_max,
        main.humidity,
        main.pressure,
        wind.speed,
        main.temp
    )
}

data class ForecastOpenWeather(
    val city: City,
    val cnt: Int,
    val cod: String,
    val list: List<Day>,
    val message: Double
) {
    data class Day(
        val clouds: Clouds,
        val dt: Int,
        val dt_txt: String,
        val main: Main,
        val rain: Rain?,
        val snow: Snow?,
        val sys: Sys,
        val weather: List<Weather>,
        val wind: Wind
    )

    data class City(
        val coord: Coord,
        val country: String,
        val id: Int,
        val name: String
    )
}

data class Wind(
    val deg: Double,
    val speed: Double
)

data class Weather(
    val description: String,
    val icon: String,
    val id: Int,
    val main: String
)

data class Sys(
    val country: String?,
    val id: Int?,
    val message: Double?,
    val sunrise: Int?,
    val sunset: Int?,
    val type: Int?,
    val pod: String?
)

data class Main(
    val grnd_level: Double?,
    val humidity: Int,
    val pressure: Double,
    val sea_level: Double?,
    val temp: Double,
    val temp_kf: Double?,
    val temp_max: Double,
    val temp_min: Double
)

data class Coord(
    val lat: Double,
    val lon: Double
)

data class Clouds(
    val all: Int
)

data class Rain(
    @Json(name = "3h") val _3h: String?
)

data class Snow(
    @Json(name = "3h") val _3h: String?
)

fun ForecastOpenWeather.asDomainModel(): List<DayWeather> {

    val forecastDaysWeather = arrayListOf<DayWeather>()

    list.map { forecastDay ->
        forecastDaysWeather.add(
            DayWeather(
                city.name,
                forecastDay.dt,
                forecastDay.weather.get(0).id,
                forecastDay.main.temp_min,
                forecastDay.main.temp_max,
                forecastDay.main.humidity,
                forecastDay.main.pressure,
                forecastDay.wind.speed,
                forecastDay.main.temp
            )
        )
    }

    return forecastDaysWeather
}