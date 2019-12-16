/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.myweather.utils

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.widget.FrameLayout
import androidx.annotation.DrawableRes
import androidx.appcompat.widget.AppCompatImageView
import com.example.myweather.R
import com.example.myweather.repository.DailyForecastItem
import com.example.myweather.repository.ForecastItem
import com.example.myweather.repository.WeatherCondition

/**
 * Contains useful utilities for a weather app, such as conversion between Celsius and Fahrenheit,
 * from kph to mph, and from degrees to NSEW.  It also contains the mapping of weather condition
 * codes in OpenWeatherMap to strings.  These strings are contained
 * Partially copied from SunshineWeatherUtils of google codelab
 */
object WeatherUtils {

    private val LOG_TAG = WeatherUtils::class.java.simpleName

    /**
     * This method will convert a temperature from Celsius to Fahrenheit.
     *
     * @param temperatureInCelsius Temperature in degrees Celsius(°C)
     *
     * @return Temperature in degrees Fahrenheit (°F)
     */
    private fun celsiusToFahrenheit(temperatureInCelsius: Double): Double {
        return temperatureInCelsius * 1.8 + 32
    }

    private fun kelvinToCelsius(temperatureInKelvin: Double): Double {
        return temperatureInKelvin - 273.15
    }

    private fun kelvinToFahrenheit(temperatureInKelvin: Double): Double {
        return temperatureInKelvin * 1.8 - 459.67
    }

    /**
     * Temperature data is stored in Celsius by our app. Depending on the user's preference,
     * the app may need to display the temperature in Fahrenheit. This method will perform that
     * temperature conversion if necessary. It will also format the temperature so that no
     * decimal points show. Temperatures will be formatted to the following form: "21°"
     *
     * @param context     Android Context to access preferences and resources
     * @param temperature Temperature in degrees Kelvin
     *
     * @return Formatted temperature String in the following form:
     * "21°"
     */
    fun formatTemperature(context: Context, temperature: Double, isMetric: Boolean): String {
        val currentTemperature =
            if (isMetric) kelvinToCelsius(temperature) else kelvinToFahrenheit(temperature)
        val temperatureFormatResourceId = R.string.format_temperature
        /* For presentation, assume the user doesn't care about tenths of a degree. */
        return String.format(context.getString(temperatureFormatResourceId), currentTemperature)
    }


    /**
     * This method will format the temperatures to be displayed in the
     * following form: "HIGH° / LOW°"
     *
     * @param context Android Context to access preferences and resources
     * @param high    High temperature for a day in user's preferred units
     * @param low     Low temperature for a day in user's preferred units
     *
     * @return String in the form: "HIGH° / LOW°"
     */
    fun formatHighLows(context: Context, high: Double, low: Double, isMetric: Boolean): String {
        val roundedHigh = Math.round(high)
        val roundedLow = Math.round(low)

        val formattedHigh = formatTemperature(context, roundedHigh.toDouble(), isMetric)
        val formattedLow = formatTemperature(context, roundedLow.toDouble(), isMetric)

        return "$formattedHigh / $formattedLow"
    }

    /**
     * This method uses the wind direction in degrees to determine compass direction as a
     * String. (eg NW) The method will return the wind String in the following form: "2 km/h SW"
     *
     * @param context   Android Context to access preferences and resources
     * @param windSpeed Wind speed in kilometers / hour
     * @param degrees   Degrees as measured on a compass, NOT temperature degrees!
     * See https://www.mathsisfun.com/geometry/degrees.html
     *
     * @return Wind String in the following form: "2 km/h SW"
     */
    fun getFormattedWind(
        context: Context,
        windSpeed: Float,
        degrees: Float,
        isMetric: Boolean
    ): String {
        var currentWindSpeed = windSpeed
        var windFormat = R.string.format_wind_kmh

        if (!isMetric) {
            windFormat = R.string.format_wind_mph
            currentWindSpeed = .621371192237334f * currentWindSpeed
        }

        /*
         * You know what's fun? Writing really long if/else statements with tons of possible
         * conditions. Seriously, try it!
         */
        var direction = "Unknown"
        if (degrees >= 337.5 || degrees < 22.5) {
            direction = "N"
        } else if (degrees >= 22.5 && degrees < 67.5) {
            direction = "NE"
        } else if (degrees >= 67.5 && degrees < 112.5) {
            direction = "E"
        } else if (degrees >= 112.5 && degrees < 157.5) {
            direction = "SE"
        } else if (degrees >= 157.5 && degrees < 202.5) {
            direction = "S"
        } else if (degrees >= 202.5 && degrees < 247.5) {
            direction = "SW"
        } else if (degrees >= 247.5 && degrees < 292.5) {
            direction = "W"
        } else if (degrees >= 292.5 && degrees < 337.5) {
            direction = "NW"
        }

        return String.format(context.getString(windFormat), currentWindSpeed, direction)
    }

    /**
     * Helper method to provide the string according to the weather
     * condition id returned by the OpenWeatherMap call.
     *
     * @param context   Android context
     * @param weatherId from OpenWeatherMap API response
     * See http://openweathermap.org/weather-conditions for a list of all IDs
     *
     * @return String for the weather condition, null if no relation is found.
     */
    fun getStringForWeatherCondition(context: Context, weatherId: Int): String {
        val stringId: Int
        if (weatherId >= 200 && weatherId <= 232) {
            stringId = R.string.condition_2xx
        } else if (weatherId >= 300 && weatherId <= 321) {
            stringId = R.string.condition_3xx
        } else
            when (weatherId) {
                500 -> stringId = R.string.condition_500
                501 -> stringId = R.string.condition_501
                502 -> stringId = R.string.condition_502
                503 -> stringId = R.string.condition_503
                504 -> stringId = R.string.condition_504
                511 -> stringId = R.string.condition_511
                520 -> stringId = R.string.condition_520
                531 -> stringId = R.string.condition_531
                600 -> stringId = R.string.condition_600
                601 -> stringId = R.string.condition_601
                602 -> stringId = R.string.condition_602
                611 -> stringId = R.string.condition_611
                612 -> stringId = R.string.condition_612
                615 -> stringId = R.string.condition_615
                616 -> stringId = R.string.condition_616
                620 -> stringId = R.string.condition_620
                621 -> stringId = R.string.condition_621
                622 -> stringId = R.string.condition_622
                701 -> stringId = R.string.condition_701
                711 -> stringId = R.string.condition_711
                721 -> stringId = R.string.condition_721
                731 -> stringId = R.string.condition_731
                741 -> stringId = R.string.condition_741
                751 -> stringId = R.string.condition_751
                761 -> stringId = R.string.condition_761
                762 -> stringId = R.string.condition_762
                771 -> stringId = R.string.condition_771
                781 -> stringId = R.string.condition_781
                800 -> stringId = R.string.condition_800
                801 -> stringId = R.string.condition_801
                802 -> stringId = R.string.condition_802
                803 -> stringId = R.string.condition_803
                804 -> stringId = R.string.condition_804
                900 -> stringId = R.string.condition_900
                901 -> stringId = R.string.condition_901
                902 -> stringId = R.string.condition_902
                903 -> stringId = R.string.condition_903
                904 -> stringId = R.string.condition_904
                905 -> stringId = R.string.condition_905
                906 -> stringId = R.string.condition_906
                951 -> stringId = R.string.condition_951
                952 -> stringId = R.string.condition_952
                953 -> stringId = R.string.condition_953
                954 -> stringId = R.string.condition_954
                955 -> stringId = R.string.condition_955
                956 -> stringId = R.string.condition_956
                957 -> stringId = R.string.condition_957
                958 -> stringId = R.string.condition_958
                959 -> stringId = R.string.condition_959
                960 -> stringId = R.string.condition_960
                961 -> stringId = R.string.condition_961
                962 -> stringId = R.string.condition_962
                else -> return context.getString(R.string.condition_unknown, weatherId)
            }

        return context.getString(stringId)
    }

    /**
     * Helper method to provide the icon resource id according to the weather condition id returned
     * by the OpenWeatherMap call. This method is very similar to
     *
     * [.getLargeArtResourceIdForWeatherCondition].
     *
     * The difference between these two methods is that this method provides smaller assets, used
     * in the list item layout for a "future day", as well as
     *
     * @param weatherId from OpenWeatherMap API response
     * See http://openweathermap.org/weather-conditions for a list of all IDs
     *
     * @return resource id for the corresponding icon. -1 if no relation is found.
     */
    fun getSmallArtResourceIdForWeatherCondition(weatherId: Int): Int {
        return WeatherCondition.valueOf(weatherId).smallIcon
    }

    /**
     * Helper method to provide the art resource ID according to the weather condition ID returned
     * by the OpenWeatherMap call. This method is very similar to
     *
     * [.getSmallArtResourceIdForWeatherCondition].
     *
     * The difference between these two methods is that this method provides larger assets, used
     * in the "today view" of the list, as well as in the DetailActivity.
     *
     * @param weatherId from OpenWeatherMap API response
     * See http://openweathermap.org/weather-conditions for a list of all IDs
     *
     * @return resource ID for the corresponding icon. -1 if no relation is found.
     */
    fun getLargeArtResourceIdForWeatherCondition(weatherId: Int): Int {
        return WeatherCondition.valueOf(weatherId).largeIcon
    }

    fun groupItemsIntoDays(list: List<ForecastItem>): Map<Long, List<ForecastItem>> {
        val dailyItemsList: HashMap<Long, List<ForecastItem>> = HashMap()
        for (numDaysInFuture in 1..5) {
            // get all items that's within the numDays in future
            val itemsWithinOneDay = list.filter { forecastItem ->

                forecastItem.date >= DateUtils.getNextDay(numDaysInFuture) && forecastItem.date < DateUtils.getNextDay(
                    numDaysInFuture + 1
                )
            }
            // save the date and all the ForecastItems within that date
            if (itemsWithinOneDay.size > 0) {
                dailyItemsList.put(DateUtils.getNextDay(numDaysInFuture), itemsWithinOneDay)
            }
        }

        return dailyItemsList
    }

    /**
     * Calculate temperature, min and max for each day
     * @param dailyMap a map of days and their forecast items within that day
     * @return a list of DailyForecastItem where each item represents a day in future
     */
    fun transformToDailyItems(dailyMap: Map<Long, List<ForecastItem>>): List<DailyForecastItem> {
        val dailyItemsList: MutableList<DailyForecastItem> = arrayListOf()
        // iterate over items of each day and find values for each day
        for ((date, items) in dailyMap) {
            val maxTemp = (items.maxBy { item -> item.temp })?.temp ?: Double.MAX_VALUE
            val minTemp = (items.minBy { item -> item.temp })?.temp ?: Double.MIN_VALUE
            val temp = items.map { item -> item.temp }.average()
            // to get the weatherId and wind, just pick a mid day item and get the value from it
            val midDayIndex = items.size / 2
            val weatherId = items.get(midDayIndex).weatherId
            val wind = items.get(midDayIndex).windSpeed
            dailyItemsList.add(
                DailyForecastItem(
                    date,
                    weatherId,
                    temp,
                    minTemp,
                    maxTemp,
                    wind,
                    items
                )
            )
        }

        dailyItemsList.sortBy { item -> item.date }
        return dailyItemsList
    }

    fun showerAnimation(context: Context, container: ViewGroup, @DrawableRes resId: Int) {
        // find container's width and height
        val containerW = container.width
        val containerH = container.height

        // add item to the view group
        val newItem = AppCompatImageView(context)
        newItem.setImageResource(resId)
        newItem.layoutParams = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.WRAP_CONTENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        container.addView(newItem)

        var itemW = newItem.width.toFloat()
        var itemH = newItem.height.toFloat()
        // scale the image to .3 to .1 of its size
        newItem.scaleX = Math.random().toFloat() * .3f + .1f
        newItem.scaleY = newItem.scaleX
        itemW *= newItem.scaleX
        itemH *= newItem.scaleY

        // position the item randomly on the horizontal axis
        newItem.translationX = Math.random().toFloat() * containerW - itemW / 2

        // move the item from top of the view to bottome view
        val mover =
            ObjectAnimator.ofFloat(newItem, View.TRANSLATION_Y, -itemH, containerH.toFloat())
        mover.interpolator = AccelerateInterpolator(1f)
        // mover.removeWhenDone(container, newStar)
        mover.repeatMode = ObjectAnimator.RESTART
        mover.repeatCount = ObjectAnimator.INFINITE
        mover.duration = (Math.random() * 1500 + 500).toLong()
        mover.start()
    }

    private fun ObjectAnimator.removeWhenDone(container: ViewGroup, view: View) {
        addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator?) {
                // container.removeView(view)
            }
        })
    }
}