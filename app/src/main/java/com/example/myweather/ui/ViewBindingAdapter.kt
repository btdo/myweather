package com.example.myweather.ui

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myweather.R
import com.example.myweather.repository.DailyForecastItem
import com.example.myweather.repository.ForecastItem
import com.example.myweather.utils.DateUtils
import com.example.myweather.utils.WeatherUtils

@BindingAdapter("temperature", "isMetric")
fun bindTemperature(view: TextView, temperature: Double, isMetric: Boolean) {
    val temp = WeatherUtils.formatTemperature(view.context, temperature, isMetric)
    view.text = temp
}

@BindingAdapter("weatherId")
fun bindWeatherId(view: ImageView, weatherId: Int) {
    val weatherImageId: Int = WeatherUtils
        .getSmallArtResourceIdForWeatherCondition(weatherId)
    view.setImageResource(weatherImageId)
}

@BindingAdapter("weatherId")
fun bindDescription(view: TextView, weatherId: Int) {
    val description = WeatherUtils.getStringForWeatherCondition(view.context, weatherId)
    /* Create the accessibility (a11y) String from the weather description */
    val descriptionA11y = view.context.getString(R.string.a11y_forecast, description)
    view.text = description
    view.contentDescription = descriptionA11y
}

@BindingAdapter("dailyList")
fun bindDailyForecastRecyclerView(
    recyclerView: RecyclerView,
    data: List<DailyForecastItem>?
) {
    val adapter = recyclerView.adapter as DailyForecastAdapter
    adapter.submitList(data)
}

@BindingAdapter("hourlyList")
fun bindHourlyForecastRecyclerView(
    recyclerView: RecyclerView,
    data: List<ForecastItem>?
) {
    val adapter = recyclerView.adapter as HourlyForecastAdapter
    adapter.submitList(data)
}

@BindingAdapter("windSpeed", "degrees", "isMetric")
fun bindWindSpeed(view: TextView, windSpeed: Float, degrees: Float, isMetric: Boolean) {
    val wind =
        WeatherUtils.getFormattedWind(view.context, windSpeed, degrees, isMetric)
    val windA11y = view.context.getString(R.string.a11y_wind, wind)
    view.text = wind
    view.contentDescription = windA11y
}

@BindingAdapter("date")
fun bindDate(view: TextView, dt: Long) {
    val text = DateUtils.getDayNameForDisplay(view.context, dt)
    view.text = text
}

@BindingAdapter("hour")
fun bindHour(view: TextView, dt: Long) {
    view.text = DateUtils.getHourForDisplay(dt)
}


@BindingAdapter("humidity")
fun bindHumidity(view: TextView, humidity: Int) {
    val humidityStr = view.context.getString(R.string.format_humidity, humidity)
    view.text = humidityStr
}

@BindingAdapter("pressure")
fun bindPressure(view: TextView, pressure: Double) {
    val pressureStr = view.context.getString(R.string.format_pressure, pressure)
    view.text = pressureStr
}

@BindingAdapter("high", "low", "isMetric")
fun bindHighLow(view: TextView, high: Double, low: Double, isMetric: Boolean) {
    val highLowStr = WeatherUtils.formatHighLows(view.context, high, low, isMetric)
    view.text = highLowStr
}