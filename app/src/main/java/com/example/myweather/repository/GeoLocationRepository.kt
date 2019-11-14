package com.example.myweather.repository

import android.location.Location
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.tasks.OnCompleteListener

interface GeoLocationRepository {

    fun locationTracking(locationCallBack: LocationCallback)

    fun getCurrentLocation(locationCallBack: OnCompleteListener<Location>)

    fun stopLocationTracking(locationCallBack: LocationCallback)

    suspend fun getAddress(location: Location): Address

}