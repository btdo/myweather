package com.example.myweather.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.myweather.databinding.DailyForecastItemBinding
import com.example.myweather.repository.ForecastItem

class DailyForecastAdapter(
    private val mContext: Context,
    var mIsMetric: Boolean,
    private var clickListener: ForecastClickListener
) :
    ListAdapter<ForecastItem, DailyForecastAdapter.DailyForecastViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DailyForecastViewHolder {
        return DailyForecastViewHolder(mContext, DailyForecastItemBinding.inflate(LayoutInflater.from(parent.context)))
    }

    override fun onBindViewHolder(holder: DailyForecastViewHolder, position: Int) {
        val dayForecast = getItem(position)
        holder.bind(dayForecast, mIsMetric)
        holder.itemView.setOnClickListener {
            clickListener.onClick(dayForecast)
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ForecastItem>() {
        override fun areItemsTheSame(oldItem: ForecastItem, newItem: ForecastItem): Boolean {
            return oldItem.date == newItem.date
        }

        override fun areContentsTheSame(oldItem: ForecastItem, newItem: ForecastItem): Boolean {
            return oldItem == newItem
        }
    }

    class DailyForecastViewHolder(private val mContext: Context, private var binding: DailyForecastItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(dayForecast: ForecastItem, isMetric: Boolean) {
            binding.dayForecast = dayForecast
            binding.isMetric = isMetric
            binding.executePendingBindings()
        }
    }
}

class ForecastClickListener(val clickListener: (day: ForecastItem) -> Unit) {
    fun onClick(forecastItem: ForecastItem) = clickListener(forecastItem)
}