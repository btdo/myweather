package com.example.myweather.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myweather.databinding.HomeDayForecastItemBinding
import com.example.myweather.repository.DayWeather

class ForecastAdapter(
    private val mContext: Context,
    var mIsMetric: Boolean,
    private var clickListener: ForecastClickListener
) :
    ListAdapter<DayWeather, ForecastAdapter.DayForecastViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayForecastViewHolder {
        return DayForecastViewHolder(mContext, HomeDayForecastItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: DayForecastViewHolder, position: Int) {
        val dayForecast = getItem(position)
        holder.bind(dayForecast, mIsMetric)
        holder.itemView.setOnClickListener {
            clickListener.onClick(dayForecast)
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<DayWeather>() {
        override fun areItemsTheSame(oldItem: DayWeather, newItem: DayWeather): Boolean {
            return oldItem.date == newItem.date
        }

        override fun areContentsTheSame(oldItem: DayWeather, newItem: DayWeather): Boolean {
            return oldItem.date == newItem.date
        }
    }

    class DayForecastViewHolder(private val mContext: Context, private var binding: HomeDayForecastItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(dayForecast: DayWeather, isMetric: Boolean) {
            binding.dayForecast = dayForecast
            binding.isMetric = isMetric
            binding.executePendingBindings()
        }
    }
}

class ForecastClickListener(val clickListener: (day: DayWeather) -> Unit) {
    fun onClick(dayWeather: DayWeather) = clickListener(dayWeather)
}