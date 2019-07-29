package com.example.myweather.ui

import android.app.Application
import androidx.lifecycle.*
import com.example.myweather.repository.ForecastItem

class DayDetailsViewModel(application: Application, forecastItem: ForecastItem) : AndroidViewModel(application) {

    private val _selectedDay = MutableLiveData<ForecastItem>()
    private val _isMetric = MutableLiveData<Boolean>()

    val isMetric: LiveData<Boolean>
        get() {
            return _isMetric
        }


    init {
        _selectedDay.value = forecastItem
        _isMetric.value = true
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

    val degrees: LiveData<Float> = Transformations.map(_selectedDay) {
        it.degrees
    }

    val humidity: LiveData<Int> = Transformations.map(_selectedDay) {
        it.humidity
    }

    val minTemp: LiveData<Double> = Transformations.map(_selectedDay) {
        it.minTemp
    }

    val maxTemp: LiveData<Double> = Transformations.map(_selectedDay) {
        it.maxTemp
    }

    val pressure: LiveData<Double> = Transformations.map(_selectedDay) {
        it.pressure
    }


}

class DayDetailsViewModelFactory(private val application: Application, private val forecastItem: ForecastItem) :
    ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DayDetailsViewModel::class.java)) {
            return DayDetailsViewModel(application, forecastItem) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}