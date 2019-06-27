package com.example.myweather.ui

import android.app.Application
import androidx.lifecycle.*
import com.example.myweather.repository.CityWeather
import com.example.myweather.repository.WeatherRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class HomeFragmentViewModel(application: Application) : AndroidViewModel(application) {
    /**
     * Event triggered for network error. This is private to avoid exposing a
     * way to set this value to observers.
     */
    private var _showNetworkError = MutableLiveData<Boolean>()

    /**
     * Event triggered for network error. Views should use this to get access
     * to the data.
     */
    val showNetworkError: LiveData<Boolean>
        get() = _showNetworkError

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

    val cityName: LiveData<String> = Transformations.map(_cityWeather) {
        it.name
    }

    val cityWeather: LiveData<CityWeather>
        get() = _cityWeather

    init {
        getWeatherForCity("Toronto")
    }

    fun getWeatherForCity(city: String) {
        viewModelScope.launch {
            try {
                val cityWeather = repository.getCityWeather(city)
                _cityWeather.value = cityWeather
                _showNetworkError.value = false
            } catch (e: Exception) {
                // Show a Toast error message and hide the progress bar.
                _showNetworkError.value = true
            }
        }
    }

    /**
     * Resets the network error flag.
     */
    fun onNetworkErrorShown() {
        _showNetworkError.value = false
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    /**
     * Factory for constructing DevByteViewModel with parameter
     */
    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeFragmentViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HomeFragmentViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }

}