package com.example.myweather.ui

import android.app.Application
import androidx.lifecycle.*
import com.example.myweather.repository.DayWeather

class DayDetailsViewModel(application: Application, dayWeather: DayWeather) : AndroidViewModel(application) {

    private val _selectedDay = MutableLiveData<DayWeather>()


    init {
        _selectedDay.value = dayWeather
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
}

class DayDetailsViewModelFactory(private val application: Application, private val dayWeather: DayWeather) :
    ViewModelProvider.Factory {

    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DayDetailsViewModel::class.java)) {
            return DayDetailsViewModel(application, dayWeather) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}