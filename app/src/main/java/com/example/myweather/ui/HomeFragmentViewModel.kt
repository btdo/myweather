package com.example.myweather.ui

import android.app.Application
import androidx.lifecycle.*
import com.example.myweather.repository.DayWeather
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
    private var _showError = MutableLiveData<Boolean>()

    /**
     * Event triggered for network error. Views should use this to get access
     * to the data.
     */
    val showError: LiveData<Boolean>
        get() = _showError

    private val _viewSelectedDay = MutableLiveData<DayWeather>()

    val viewSelectedDay: LiveData<DayWeather>
        get() = _viewSelectedDay



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

    private val _todayWeather = MutableLiveData<DayWeather>()
    private val _forecastDays = MutableLiveData<List<DayWeather>>()

    val cityName: LiveData<String> = Transformations.map(_todayWeather) {
        it.city
    }

    val temperature: LiveData<Double> = Transformations.map(_todayWeather) {
        it.temp
    }

    val weatherId: LiveData<Int> = Transformations.map(_todayWeather) {
        it.weatherId
    }

    val forecastDays: LiveData<List<DayWeather>>
        get() {
            return _forecastDays
        }

    private val _isMetric = MutableLiveData<Boolean>()

    val isMetric: LiveData<Boolean>
        get() {
            return _isMetric
        }

    init {
        getTodayWeather("Toronto")
        getForecastWeather("Toronto")
        _isMetric.value = true
    }

    fun viewSelectedDay(dayWeather: DayWeather) {
        _viewSelectedDay.value = dayWeather
    }

    fun viewSelectedDayComplete() {
        _viewSelectedDay.value = null
    }

    private fun getTodayWeather(city: String) {
        viewModelScope.launch {
            try {
                val cityWeather = repository.getTodayWeather(city)
                _todayWeather.value = cityWeather
                _showError.value = false
            } catch (e: Exception) {
                // Show a Toast error message and hide the progress bar.
                _showError.value = true
            }
        }
    }

    private fun getForecastWeather(city: String) {
        viewModelScope.launch {
            try {
                val forecastWeather = repository.getForecastWeather(city)
                _forecastDays.value = forecastWeather
                _showError.value = false
            } catch (e: Exception) {
                // Show a Toast error message and hide the progress bar.
                _showError.value = true
            }
        }
    }

    /**
     * Resets the network error flag.
     */
    fun onNetworkErrorShown() {
        _showError.value = false
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

    /**
     * Factory for constructing DevByteViewModel with parameter
     */
    class Factory(private val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeFragmentViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HomeFragmentViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }

}