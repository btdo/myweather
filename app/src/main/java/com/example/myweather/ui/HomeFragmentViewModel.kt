package com.example.myweather.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myweather.repository.CityWeather
import com.example.myweather.repository.WeatherRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.IOException

class HomeFragmentViewModel(application: Application) : AndroidViewModel(application) {


    /**
     * Event triggered for network error. This is private to avoid exposing a
     * way to set this value to observers.
     */
    private var _eventNetworkError = MutableLiveData<Boolean>()

    /**
     * Event triggered for network error. Views should use this to get access
     * to the data.
     */
    val eventNetworkError: LiveData<Boolean>
        get() = _eventNetworkError

    /**
     * This is the job for all coroutines started by this ViewModel.
     *
     * Cancelling this job will cancel all coroutines started by this ViewModel.
     */
    private val viewModelJob = SupervisorJob()

    /**
     * This is the main scope for all coroutines launched by MainViewModel.
     *
     * Since we pass viewModelJob, you can cancel all coroutines launched by uiScope by calling
     * viewModelJob.cancel()
     */
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val repository = WeatherRepository()

    private val _cityWeather = MutableLiveData<CityWeather>()

    val cityWeather: LiveData<CityWeather>
        get() = _cityWeather

    fun getWeatherForCity(city: String) {
        viewModelScope.launch {
            try {
                val playlist = repository.getCityWeather(city)
                _eventNetworkError.value = false

            } catch (networkError: IOException) {
                // Show a Toast error message and hide the progress bar.
                _eventNetworkError.value = true
            }
        }
    }

}