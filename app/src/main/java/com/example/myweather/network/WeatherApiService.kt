package com.example.myweather.network

import com.example.myweather.BuildConfig
import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Query



// production api
private const val BASE_URL = BuildConfig.BASE_URL
private const val DEFAULT_APPID = BuildConfig.DEFAULT_APPID

/* The DEFAULT_UNITS we want our API to return */
private const val DEFAULT_UNITS = "metric"
private const val IMPERIAL_UNITS = "imperial"
/* The number of days we want our API to return */
private const val DEFAULT_NUMDAYS = 5
private const val DEFAULT_FORMAT = "json"
/* The query parameter allows us to provide a location string to the API */
private const val QUERY_PARAM = "q"
private const val FORMAT_PARAM = "mode"
/* The DEFAULT_UNITS parameter allows us to designate whether we want metric DEFAULT_UNITS or imperial DEFAULT_UNITS */
private const val UNITS_PARAM = "units"
/* The days parameter allows us to designate how many days of weather data we want */
private const val DAYS_PARAM = "cnt"
private const val APPID_PARAM = "appid"


private val retrofit = setupRetrofit(BASE_URL)

interface WeatherApiService {
    @GET("weather")
    fun getCurrentWeather(
        @Query(QUERY_PARAM) location: String, @Query(APPID_PARAM) appid: String = DEFAULT_APPID
    ): Deferred<TodayOpenWeather>

    @GET("forecast")
    fun getDailyForecast(
        @Query(QUERY_PARAM) location: String, @Query(APPID_PARAM) appid: String = DEFAULT_APPID
    ): Deferred<DailyForecastOpenWeather>

    @GET("forecast/hourly")
    fun getHourlyForecast(
        @Query(QUERY_PARAM) location: String, @Query(APPID_PARAM) appid: String = DEFAULT_APPID
    ): Deferred<HourlyForecastOpenWeather>
}

object WeatherApi {
    val weatherService: WeatherApiService by lazy {
        retrofit.create(WeatherApiService::class.java)
    }
}