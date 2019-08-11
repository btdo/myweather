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

import android.content.Context
import com.example.myweather.R
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
/**
 * Class for handling date conversions that are useful for converting date from backend to database and from database to model for display
 */
object SunshineDateUtils {

    fun normalizeDateTime(dt: Long): Long {
        return dt * 1000
    }

    fun getHourForDisplay(dateTime: Long): String {
        val localizedHour = SimpleDateFormat("haaa").format(dateTime)
        return localizedHour
    }

    /**
     * Get the current hour of today's date to save in database and indicate the hour the the API was called
     */
    fun getCurrentHour(): Long {
        val utcNowMillis = System.currentTimeMillis()
        val hoursSinceEpochLocal = TimeUnit.MILLISECONDS.toHours(utcNowMillis)
        return TimeUnit.HOURS.toMillis(hoursSinceEpochLocal)
    }

    fun getNextDaysNoon(daysCount: Int): Long {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.add(Calendar.DATE, daysCount) // Add daysCount days to current date
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.HOUR, 0)
        calendar.set(Calendar.AM_PM, Calendar.PM)
        return calendar.timeInMillis
    }

    /**
     * Given a day, returns just the name to use for that day.
     * E.g "today", "tomorrow", "Wednesday".
     *
     * @param context      Context to use for resource localization
     * @param dateInMillis The date in milliseconds (UTC time)
     *
     * @return the string day of the week
     */
    fun getDayNameForDisplay(context: Context, dateInMillis: Long): String {
        /*
         * If the date is today, return the localized version of "Today" instead of the actual
         * day name.
         */
        when (getNumDaysBetweenNow(dateInMillis)) {
            0 -> return context.getString(R.string.today)
            1 -> return context.getString(R.string.tomorrow)
            else -> {
                val dayFormat = SimpleDateFormat("EEEE")
                return dayFormat.format(dateInMillis)
            }
        }
    }

    fun getNumDaysBetweenNow(dateInMillis: Long): Int {
        val difference = dateInMillis - System.currentTimeMillis()
        val daysBetween = TimeUnit.DAYS.convert(difference, TimeUnit.MILLISECONDS)
        return daysBetween.toInt() + 1
    }
}