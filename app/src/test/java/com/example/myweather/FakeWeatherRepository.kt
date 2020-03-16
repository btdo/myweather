package com.example.myweather

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.myweather.network.Location
import com.example.myweather.repository.ForecastItem
import com.example.myweather.repository.WeatherRepository

class FakeWeatherRepository : WeatherRepository {

    private val _todayForecast = MutableLiveData<ForecastItem>()
    override val todayForecast: LiveData<ForecastItem>
        get() = _todayForecast

    private val _forecast = MutableLiveData<List<ForecastItem>>()
    override val forecast: LiveData<List<ForecastItem>>
        get() = _forecast

    var todayForecastResult: ForecastItem? = null
    var forecastResult: List<ForecastItem>? = null

    override suspend fun clearCache() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override suspend fun getTodayForecast(
        location: String,
        isForcedRefresh: Boolean
    ): ForecastItem {
        _todayForecast.value = todayForecastResult
        return todayForecastResult!!
    }

    override suspend fun getComingDaysForecast(
        location: String,
        isForcedRefresh: Boolean
    ): List<ForecastItem> {
        _forecast.value = forecastResult
        return forecastResult!!
    }

    override suspend fun getLocation(cityName: String): List<Location> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}