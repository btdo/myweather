package com.example.myweather.ui

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.example.myweather.utils.SunshineWeatherUtils

@BindingAdapter("temperature", "isMetric")
fun setTemperature(view: TextView, temperature: Double, isMetric: Boolean) {
    val temp = SunshineWeatherUtils.formatTemperature(view.context, temperature, isMetric)
    view.text = temp
}