package com.example.myweather.utils

import com.example.myweather.repository.ForecastItem
import java.util.*


fun generateForecastItem(location: String, itemDate: Calendar): ForecastItem {
    return ForecastItem(
        location,
        itemDate.timeInMillis,
        500,
        12.0,
        18.0,
        70,
        101.0,
        12f,
        43f,
        15.0,
        "Raining",
        "Light rain"
    )
}

fun generateListOfForecastItems(location: String): List<ForecastItem> {
    val list = mutableListOf<ForecastItem>()
    for (n in 0..40) {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.add(Calendar.HOUR, n * 3) // Add 3 hour interval
        list.add(generateForecastItem(location, calendar))
    }
    return list
}