package com.example.myweather.network

import com.example.myweather.database.ForecastItemEntity
import com.example.myweather.repository.ForecastItem
import com.example.myweather.utils.DateUtils
import com.squareup.moshi.Json

data class TodayOpenWeather(
    val base: String,
    val clouds: Clouds,
    val cod: Int,
    val coord: Coord,
    val dt: Long,
    val id: Int,
    val main: Main,
    val name: String,
    val sys: Sys,
    val visibility: Int,
    val weather: List<Weather>,
    val wind: Wind
)

fun TodayOpenWeather.asDomainModel(): ForecastItem {
    return ForecastItem(
        name.trim().toLowerCase() + "," + sys.country?.trim()?.toLowerCase(),
        dt,
        weather[0].id,
        main.temp_min,
        main.temp_max,
        main.humidity,
        main.pressure,
        wind.speed,
        wind.deg ?: 0f,
        main.temp,
        weather.get(0).main,
        weather.get(0).description
    )
}

fun TodayOpenWeather.asDatabaseModel(): ForecastItemEntity {
    return ForecastItemEntity(
        name.trim().toLowerCase() + "," + sys.country?.trim()?.toLowerCase(),
        DateUtils.getCurrentHour(),
        weather[0].id,
        main.temp_min,
        main.temp_max,
        main.humidity,
        main.pressure,
        wind.speed,
        wind.deg ?: 0f,
        main.temp,
        weather.get(0).main,
        weather.get(0).description
    )
}

data class DailyForecastOpenWeather(
    val city: Location,
    val cnt: Int,
    val cod: String,
    val list: List<Day>,
    val message: Double
)

data class Day(
    val clouds: Clouds,
    val dt: Long,
    val dt_txt: String,
    val main: Main,
    val rain: Rain?,
    val snow: Snow?,
    val sys: Sys,
    val weather: List<Weather>,
    val wind: Wind
)

data class Wind(
    val deg: Float?,
    val speed: Float
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

fun DailyForecastOpenWeather.asDomainModel(): List<ForecastItem> {
    val forecastDaysWeather = arrayListOf<ForecastItem>()
    list.map { forecastDay ->
        forecastDaysWeather.add(
            ForecastItem(
                city.name.trim().toLowerCase() + "," + city.country.trim().toLowerCase(),
                DateUtils.normalizeDateTime(forecastDay.dt),
                forecastDay.weather.get(0).id,
                forecastDay.main.temp_min,
                forecastDay.main.temp_max,
                forecastDay.main.humidity,
                forecastDay.main.pressure,
                forecastDay.wind.speed,
                forecastDay.wind.deg ?: 0f,
                forecastDay.main.temp,
                forecastDay.weather.get(0).main,
                forecastDay.weather.get(0).description
            )
        )
    }

    return forecastDaysWeather
}

fun DailyForecastOpenWeather.asDatabaseModel(): List<ForecastItemEntity> {
    val forecastDaysWeather = arrayListOf<ForecastItemEntity>()
    list.map { forecastDay ->
        forecastDaysWeather.add(
            ForecastItemEntity(
                city.name.trim().toLowerCase() + "," + city.country.trim().toLowerCase(),
                DateUtils.normalizeDateTime(forecastDay.dt),
                forecastDay.weather.get(0).id,
                forecastDay.main.temp_min,
                forecastDay.main.temp_max,
                forecastDay.main.humidity,
                forecastDay.main.pressure,
                forecastDay.wind.speed,
                forecastDay.wind.deg ?: 0f,
                forecastDay.main.temp,
                forecastDay.weather.get(0).main,
                forecastDay.weather.get(0).description
            )
        )
    }

    return forecastDaysWeather
}

data class Location(
    val coord: Coord,
    val country: String,
    val id: Int,
    val name: String,
    val population: Int?
)

data class HourlyForecastOpenWeather(
    val city: Location,
    val cnt: Int,
    val cod: String,
    val list: List<HourlyOpenWeather>,
    val message: Double
)

data class HourlyOpenWeather(
    val clouds: Clouds,
    val dt: Long,
    val dt_txt: String,
    val main: Main,
    val rain: Rain,
    val sys: Sys,
    val weather: List<Weather>,
    val wind: Wind
)
