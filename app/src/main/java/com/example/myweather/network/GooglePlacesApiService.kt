package com.example.myweather.network

import com.example.myweather.BuildConfig
import kotlinx.coroutines.Deferred
import retrofit2.http.GET
import retrofit2.http.Query

// production api
private const val BASE_URL = BuildConfig.BASE_URL
private const val DEFAULT_APPID = BuildConfig.DEFAULT_APPID

/* The DEFAULT_UNITS we want our API to return */
private const val QUERY_PARAM = "query"
private const val QUERY_APPID_PARAM = "key"

private const val QUERY_TYPES_PARAM = "types"
private const val QUERY_TYPES_PARAM_VALUE = "(cities)"

private val retrofit = setupRetrofit(BASE_URL)

interface GooglePlacesApiService {

    @GET()
    fun getPlaces(
        @Query(QUERY_PARAM) location: String,
        @Query(QUERY_TYPES_PARAM) types: String = QUERY_TYPES_PARAM_VALUE,
        @Query(QUERY_APPID_PARAM) appid: String = DEFAULT_APPID
    ): Deferred<TodayOpenWeather>

}

object GooglePlacesApi {
    val service: GooglePlacesApiService by lazy {
        retrofit.create(GooglePlacesApiService::class.java)
    }
}
