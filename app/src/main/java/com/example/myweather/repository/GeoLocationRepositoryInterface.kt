package com.example.myweather.repository

import android.location.Location
import com.google.android.gms.location.LocationCallback

interface GeoLocationRepositoryInterface {

    fun startTrackingByLocation(locationCallBack: LocationCallback)

    fun stopTrackingByLocation(locationCallBack: LocationCallback)

    suspend fun getAddress(location: Location): Address

}