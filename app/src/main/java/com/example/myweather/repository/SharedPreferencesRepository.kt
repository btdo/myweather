package com.example.myweather.repository

interface SharedPreferencesRepository {
    fun isLocationTrackingEnabled(): Boolean
    fun getDefaultLocation(): String
    fun isHourlySyncEnabled(): Boolean
    fun isMetricUnit(): Boolean

    fun setLocationTracking(isTracking: Boolean)
}