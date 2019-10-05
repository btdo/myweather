package com.example.myweather.repository

import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.*

class GeoLocationRepository(private val mContext: Context) : GeoLocationRepositoryInterface {

    private var mFusedLocationClient: FusedLocationProviderClient? = null

    override fun startTrackingByLocation(locationCallBack: LocationCallback) {
        try {
            if (mFusedLocationClient == null) {
                mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext)
            }

            mFusedLocationClient?.requestLocationUpdates(
                getLocationRequest(), locationCallBack,
                null /* Looper */
            )
        } catch (e: SecurityException) {
            throw e
        }
    }

    override fun stopTrackingByLocation(locationCallBack: LocationCallback) {
        mFusedLocationClient?.removeLocationUpdates(locationCallBack)
    }

    override suspend fun getAddress(location: Location): Address = withContext(Dispatchers.Default) {
        val geoCoder = Geocoder(mContext, Locale.getDefault())
        val addresses = geoCoder.getFromLocation(location.latitude, location.longitude, 1)
        if (addresses == null || addresses.size == 0) {
            throw IOException("Address not found")
        }

        val address = addresses.get(0)
        val addressParts = mutableListOf<String>()
        for (i in 0..address.maxAddressLineIndex) {
            addressParts.add(address.getAddressLine(i))
        }

        return@withContext Address(address.locality, address.countryCode, location.latitude, location.longitude)
    }

    private fun getLocationRequest(): LocationRequest {
        val locationRequest = LocationRequest()
        locationRequest.interval = 100000
        locationRequest.fastestInterval = 50000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        return locationRequest
    }
}