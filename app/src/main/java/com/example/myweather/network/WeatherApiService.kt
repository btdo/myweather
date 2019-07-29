package com.example.myweather.network

import com.example.myweather.BuildConfig
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Deferred
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

// production api
//private const val BASE_URL = "https://api.openweathermap.org/data/2.5/"
//private const val DEFAULT_APPID = "bb3baa6b163be4873d04a8224b53b145"

// development
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


private val retrofit = setupRetrofit()

fun setupRetrofit(): Retrofit {
    val logging = HttpLoggingInterceptor()
    logging.level = HttpLoggingInterceptor.Level.BODY
    val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()
    val retrofit = Retrofit.Builder()
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .client(client)
        .baseUrl(BASE_URL)
        .build()
    return retrofit
}

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

interface WeatherApiServiceProd {

    @GET("weather")
    fun getCurrentWeather(
        @Query(QUERY_PARAM) location: String, @Query(APPID_PARAM) appid: String = DEFAULT_APPID, @Query(UNITS_PARAM) units: String = DEFAULT_UNITS
    ): Deferred<TodayOpenWeather>

    @GET("forecast")
    fun getDailyForecast(
        @Query(QUERY_PARAM) location: String, @Query(APPID_PARAM) appid: String = DEFAULT_APPID, @Query(DAYS_PARAM) cnt: Int = DEFAULT_NUMDAYS, @Query(
            UNITS_PARAM
        ) units: String = DEFAULT_UNITS
    ): Deferred<DailyForecastOpenWeather>

    @GET("forecast/hourly")
    fun getHourlyForecast(
        @Query(QUERY_PARAM) location: String, @Query(APPID_PARAM) appid: String = DEFAULT_APPID, @Query(UNITS_PARAM) units: String = DEFAULT_UNITS
    ): Deferred<HourlyForecastOpenWeather>
}

object WeatherApi {
    val weatherService: WeatherApiService by lazy {
        retrofit.create(WeatherApiService::class.java)
    }
    val weatherServiceProd: WeatherApiServiceProd by lazy {
        retrofit.create(WeatherApiServiceProd::class.java)
    }
}