package com.example.myweather.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myweather.R
import com.example.myweather.databinding.HomeDayForecastItemBinding
import com.example.myweather.repository.DayWeather
import com.example.myweather.utils.SunshineDateUtils
import com.example.myweather.utils.SunshineWeatherUtils

class ForecastAdapter(private val mContext: Context, var mIsMetric: Boolean, var clickListener: ForecastClickListener) :
    ListAdapter<DayWeather, ForecastAdapter.DayForecastViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayForecastViewHolder {
        return DayForecastViewHolder(mContext, HomeDayForecastItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: DayForecastViewHolder, position: Int) {
        val dayForecast = getItem(position)
        holder.bind(dayForecast)
    }

    companion object DiffCallback : DiffUtil.ItemCallback<DayWeather>() {
        override fun areItemsTheSame(oldItem: DayWeather, newItem: DayWeather): Boolean {
            return oldItem.date == newItem.date
        }

        override fun areContentsTheSame(oldItem: DayWeather, newItem: DayWeather): Boolean {
            return oldItem.date == newItem.date
        }
    }


    inner class DayForecastViewHolder(private val mContext: Context, private var binding: HomeDayForecastItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(dayForecast: DayWeather) {

            binding.dayForecastItem.setOnClickListener {
                clickListener.onClick(dayForecast)
            }

            val weatherImageId: Int = SunshineWeatherUtils
                .getSmallArtResourceIdForWeatherCondition(dayForecast.weatherId)

            binding.weatherIcon.setImageResource(weatherImageId)

            val dateInMillis = dayForecast.date
            /* Get human readable string using our utility method */
            val dateString = SunshineDateUtils.getFriendlyDateString(mContext, dateInMillis, false)
            binding.forecastDate.text = dateString

            val description = SunshineWeatherUtils.getStringForWeatherCondition(mContext, dayForecast.weatherId)
            /* Create the accessibility (a11y) String from the weather description */
            val descriptionA11y = mContext.getString(R.string.a11y_forecast, description)
            binding.forecastCondition.text = description
            binding.forecastCondition.contentDescription = descriptionA11y

            val temp = SunshineWeatherUtils.formatTemperature(mContext, dayForecast.temp, mIsMetric)
            /* Create the accessibility (a11y) String from the weather description */
            val tempA11y = mContext.getString(R.string.a11y_temp, temp)
            /* Set the text and content description (for accessibility purposes) */
            binding.forecastTemp.text = temp
            binding.forecastTemp.contentDescription = tempA11y

            val wind =
                SunshineWeatherUtils.getFormattedWind(mContext, dayForecast.windSpeed, dayForecast.degrees, mIsMetric)
            val windA11y = mContext.getString(R.string.a11y_wind, temp)
            binding.forecastWindspeed.text = wind
            binding.forecastWindspeed.contentDescription = windA11y

        }
    }
}

class ForecastClickListener(val clickListener: (day: DayWeather) -> Unit) {
    fun onClick(dayWeather: DayWeather) = clickListener(dayWeather)
}