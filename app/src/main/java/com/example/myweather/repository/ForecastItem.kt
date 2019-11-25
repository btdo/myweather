package com.example.myweather.repository

import android.os.Parcelable
import androidx.annotation.DrawableRes
import com.example.myweather.R
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ForecastItem(
    val location: String,
    val date: Long,
    val weatherId: Int,
    val minTemp: Double,
    val maxTemp: Double,
    val humidity: Int,
    val pressure: Double,
    val windSpeed: Float,
    val degrees: Float,
    val temp: Double,
    val mainDescription: String,
    val description: String
) : Parcelable

@Parcelize
data class DailyForecastItem(
    val date: Long,
    val weatherId: Int,
    val temp: Double,
    val minTemp: Double,
    val maxTemp: Double,
    val windSpeed: Float,
    val hourlyItems: List<ForecastItem>
) : Parcelable


enum class WeatherCondition(
    val largeIcon: Int,
    val smallIcon: Int,
    val animationSettings: AnimationSettings?
) {
    storm(
        R.drawable.art_storm,
        R.drawable.ic_storm,
        AnimationSettings(R.drawable.ic_raindrop, 150)
    ),
    light_rain(
        R.drawable.art_light_rain,
        R.drawable.ic_light_rain,
        AnimationSettings(R.drawable.ic_raindrop, 50)
    ),
    rain(R.drawable.art_rain, R.drawable.ic_rain, AnimationSettings(R.drawable.ic_raindrop, 100)),
    snow(R.drawable.art_snow, R.drawable.ic_snow, AnimationSettings(R.drawable.ic_snowflake, 100)),
    fog(R.drawable.art_fog, R.drawable.ic_fog, null),
    clear(R.drawable.art_clear, R.drawable.ic_clear, null),
    light_clouds(R.drawable.art_light_clouds, R.drawable.ic_light_clouds, null),
    clouds(R.drawable.art_clouds, R.drawable.ic_cloudy, null);

    class AnimationSettings(@DrawableRes val drawableId: Int, val volume: Int)

    companion object {
        fun valueOf(weatherId: Int): WeatherCondition {
            if (weatherId >= 200 && weatherId <= 232) {
                return storm
            } else if (weatherId >= 300 && weatherId <= 321) {
                return light_rain
            } else if (weatherId >= 500 && weatherId <= 504) {
                return rain
            } else if (weatherId == 511) {
                return snow
            } else if (weatherId >= 520 && weatherId <= 531) {
                return rain
            } else if (weatherId >= 600 && weatherId <= 622) {
                return snow
            } else if (weatherId >= 701 && weatherId <= 761) {
                return fog
            } else if (weatherId == 761 || weatherId == 771 || weatherId == 781) {
                return storm
            } else if (weatherId == 800) {
                return clear
            } else if (weatherId == 801) {
                return light_clouds
            } else if (weatherId >= 802 && weatherId <= 804) {
                return clouds
            } else if (weatherId >= 900 && weatherId <= 906) {
                return storm
            } else if (weatherId >= 958 && weatherId <= 962) {
                return storm
            } else if (weatherId >= 951 && weatherId <= 957) {
                return clear
            }
            return storm
        }
    }

}