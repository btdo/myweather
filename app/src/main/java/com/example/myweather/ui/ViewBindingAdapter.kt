package com.example.myweather.ui

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myweather.R
import com.example.myweather.repository.ForecastItem
import com.example.myweather.utils.SunshineDateUtils
import com.example.myweather.utils.SunshineWeatherUtils

@BindingAdapter("temperature", "isMetric")
fun bindTemperature(view: TextView, temperature: Double, isMetric: Boolean) {
    val temp = SunshineWeatherUtils.formatTemperature(view.context, temperature, isMetric)
    view.text = temp
}

@BindingAdapter("weatherId")
fun bindWeatherId(view: ImageView, weatherId: Int) {
    val weatherImageId: Int = SunshineWeatherUtils
        .getSmallArtResourceIdForWeatherCondition(weatherId)
    view.setImageResource(weatherImageId)
}

@BindingAdapter("weatherId")
fun bindDescription(view: TextView, weatherId: Int) {
    val description = SunshineWeatherUtils.getStringForWeatherCondition(view.context, weatherId)
    /* Create the accessibility (a11y) String from the weather description */
    val descriptionA11y = view.context.getString(R.string.a11y_forecast, description)
    view.text = description
    view.contentDescription = descriptionA11y
}

@BindingAdapter("listData")
fun bindRecyclerView(
    recyclerView: RecyclerView,
    data: List<ForecastItem>?
) {
    val adapter = recyclerView.adapter as ForecastAdapter
    adapter.submitList(data)
}

@BindingAdapter("windSpeed", "degrees", "isMetric")
fun bindWindSpeed(view: TextView, windSpeed: Float, degrees: Float, isMetric: Boolean) {
    val wind =
        SunshineWeatherUtils.getFormattedWind(view.context, windSpeed, degrees, isMetric)
    val windA11y = view.context.getString(R.string.a11y_wind, wind)
    view.text = wind
    view.contentDescription = windA11y
}

@BindingAdapter("date")
fun bindDate(view: TextView, dt: Long) {
    view.text = SunshineDateUtils.getFriendlyDateString(view.context, dt, false)
    val test = SunshineDateUtils.getFriendlyDateHourString(view.context, dt)
}

@BindingAdapter("hour")
fun bindHour(view: TextView, dt: Long) {
    view.text = SunshineDateUtils.getFriendlyDateHourString(view.context, dt)
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
    val highLowStr = SunshineWeatherUtils.formatHighLows(view.context, high, low, isMetric)
    view.text = highLowStr
}