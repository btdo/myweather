package com.example.myweather.ui

import android.app.Application
import androidx.lifecycle.*
import com.example.myweather.repository.DailyForecastItem
import com.example.myweather.repository.ForecastItem

class DayDetailsViewModel(application: Application, forecastItem: DailyForecastItem, isMetric: Boolean) :
    AndroidViewModel(application) {

    private val _selectedDay = MutableLiveData<DailyForecastItem>()
    private val _isMetric = MutableLiveData<Boolean>()

    val isMetric: LiveData<Boolean>
        get() {
            return _isMetric
        }


    init {
        _selectedDay.value = forecastItem
        _isMetric.value = isMetric
    }

    val temperature: LiveData<Double> = Transformations.map(_selectedDay) {
        it.temp
    }

    val weatherId: LiveData<Int> = Transformations.map(_selectedDay) {
        it.weatherId
    }

    val date: LiveData<Long> = Transformations.map(_selectedDay) {
        it.date
    }

    val windSpeed: LiveData<Float> = Transformations.map(_selectedDay) {
        it.windSpeed
    }

    val minTemp: LiveData<Double> = Transformations.map(_selectedDay) {
        it.minTemp
    }

    val maxTemp: LiveData<Double> = Transformations.map(_selectedDay) {
        it.maxTemp
    }

    val hourlyForecast: LiveData<List<ForecastItem>> = Transformations.map(_selectedDay) {
        it.hourlyItems
    }

}

class DayDetailsViewModelFactory(
    private val application: Application,
    private val forecastItem: DailyForecastItem,
    private val isMetric: Boolean
) :
    ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DayDetailsViewModel::class.java)) {
            return DayDetailsViewModel(application, forecastItem, isMetric) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}