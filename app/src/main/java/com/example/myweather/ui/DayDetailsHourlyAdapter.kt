package com.example.myweather.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myweather.databinding.DayDetailsHourlyForecastItemBinding
import com.example.myweather.repository.ForecastItem

class DayDetailsHourlyAdapter(var mIsMetric: Boolean) :
    ListAdapter<ForecastItem, DayDetailsHourlyAdapter.DayDetailsHourlyViewHolder>(DiffCallBack) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayDetailsHourlyViewHolder {
        return DayDetailsHourlyViewHolder(DayDetailsHourlyForecastItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: DayDetailsHourlyViewHolder, position: Int) {
        val hourlyForecast = getItem(position)
        holder.bind(hourlyForecast, mIsMetric)
    }

    companion object DiffCallBack : DiffUtil.ItemCallback<ForecastItem>() {
        override fun areItemsTheSame(oldItem: ForecastItem, newItem: ForecastItem): Boolean {
            return oldItem.date == newItem.date
        }

        override fun areContentsTheSame(oldItem: ForecastItem, newItem: ForecastItem): Boolean {
            return oldItem == newItem
        }
    }

    class DayDetailsHourlyViewHolder(private var binding: DayDetailsHourlyForecastItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(hourlyForecast: ForecastItem, isMetric: Boolean) {
            binding.hourlyForecast = hourlyForecast
            binding.isMetric = isMetric
            binding.executePendingBindings()
        }
    }

}