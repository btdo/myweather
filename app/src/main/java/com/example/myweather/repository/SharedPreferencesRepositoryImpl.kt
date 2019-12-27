package com.example.myweather.repository

import android.content.Context
import androidx.preference.PreferenceManager
import com.example.myweather.R
import javax.inject.Inject


class SharedPreferencesRepositoryImpl @Inject constructor(mContext: Context) :
    SharedPreferencesRepository {

    val sharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(mContext)

    val resources = mContext.resources

    override fun isLocationTrackingEnabled(): Boolean =
        sharedPreferences.getBoolean(
            resources.getString(R.string.pref_enable_geo_location_key),
            false
        )

    override fun getDefaultLocation(): String =
        sharedPreferences.getString(
            resources.getString(R.string.pref_location_key),
            resources.getString(R.string.pref_location_default)
        )!!

    override fun isHourlySyncEnabled(): Boolean =
        sharedPreferences.getBoolean(resources.getString(R.string.pref_hourly_sync_key), false)

    override fun isMetricUnit(): Boolean =
        sharedPreferences.getString(
            resources.getString(R.string.pref_units_key),
            "metric"
        ) == resources.getString(R.string.pref_units_metric)


    override fun setLocationTracking(isTracking: Boolean) {
        val editor = sharedPreferences.edit()
        editor.putBoolean(
            resources.getString(R.string.pref_enable_geo_location_key),
            isTracking
        )
        editor.apply()
    }

    override fun isLocationDBPopulated(): Boolean {
        return sharedPreferences.getBoolean(
            resources.getString(R.string.pref_location_db_populated_key),
            false
        )
    }

    override fun markLocationDBPopulated() {
        val editor = sharedPreferences.edit()
        editor.putBoolean(
            resources.getString(R.string.pref_location_db_populated_key),
            true
        )
        editor.apply()
    }
}