package com.example.myweather.ui

import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myweather.repository.DayWeather
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

@BindingAdapter("listData")
fun bindRecyclerView(
    recyclerView: RecyclerView,
    data: List<DayWeather>?
) {
    val adapter = recyclerView.adapter as ForecastAdapter
    adapter.submitList(data)
}