package com.example.myweather.network

import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private const val BASE_URL = "https://andfun-weather.udacity.com/"
/* The DEFAULT_UNITS we want our API to return */
private const val DEFAULT_UNITS = "metric"
/* The number of days we want our API to return */
private const val DEFAULT_NUMDAYS = 14
private const val DEFAULT_FORMAT = "json"

/* The query parameter allows us to provide a location string to the API */
private const val QUERY_PARAM = "q"
private const val FORMAT_PARAM = "mode"
/* The DEFAULT_UNITS parameter allows us to designate whether we want metric DEFAULT_UNITS or imperial DEFAULT_UNITS */
private const val UNITS_PARAM = "DEFAULT_UNITS"
/* The days parameter allows us to designate how many days of weather data we want */
private const val DAYS_PARAM = "cnt"


private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .baseUrl(BASE_URL)
    .build()

interface WeatherApiService {
    @GET("weather")
    fun getWeatherForCity(
        @Query(QUERY_PARAM) location: String, @Query(FORMAT_PARAM) format: String = DEFAULT_FORMAT, @Query(UNITS_PARAM) units: String = DEFAULT_UNITS, @Query(
            DAYS_PARAM
        ) days: Int = DEFAULT_NUMDAYS
    ):  Deferred<NetworkCityWeather>
}

object WeatherApi {
    val weatherService: WeatherApiService by lazy {
        retrofit.create(WeatherApiService::class.java)
    }
}