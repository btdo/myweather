package com.example.myweather.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myweather.databinding.HourlyForecastItemBinding
import com.example.myweather.repository.ForecastItem

class HourlyForecastAdapter(
    private val mContext: Context,
    var mIsMetric: Boolean,
    private var clickListener: HourlyForecastClickListener? = null
) :
    ListAdapter<ForecastItem, HourlyForecastAdapter.HourlyForecastViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HourlyForecastViewHolder {
        return HourlyForecastViewHolder(
            mContext,
            HourlyForecastItemBinding.inflate(LayoutInflater.from(parent.context))
        )
    }

    override fun onBindViewHolder(holder: HourlyForecastViewHolder, position: Int) {
        val hourForecast = getItem(position)
        holder.bind(hourForecast, mIsMetric)
        holder.itemView.setOnClickListener {
            clickListener?.onClick(hourForecast)
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ForecastItem>() {
        override fun areItemsTheSame(oldItem: ForecastItem, newItem: ForecastItem): Boolean {
            return oldItem.date == newItem.date
        }

        override fun areContentsTheSame(oldItem: ForecastItem, newItem: ForecastItem): Boolean {
            return oldItem.date == newItem.date
        }
    }

    class HourlyForecastViewHolder(private val mContext: Context, private var binding: HourlyForecastItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(hourForecast: ForecastItem, isMetric: Boolean) {
            binding.hourlyForecast = hourForecast
            binding.isMetric = isMetric
            binding.executePendingBindings()
        }
    }
}

class HourlyForecastClickListener(val clickListener: (hour: ForecastItem) -> Unit) {
    fun onClick(forecastItem: ForecastItem) = clickListener(forecastItem)
}