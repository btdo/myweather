package com.example.myweather.ui

import android.app.Application
import androidx.lifecycle.*
import com.example.myweather.repository.ForecastItem
import com.example.myweather.repository.HourForecast
import com.example.myweather.repository.WeatherRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class HomeFragmentViewModel(application: Application, initLocation: String) : AndroidViewModel(application) {
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

    private val _viewSelectedDay = MutableLiveData<ForecastItem>()

    val viewSelectedDay: LiveData<ForecastItem>
        get() = _viewSelectedDay

    private val _location = MutableLiveData<String>()


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

    private val _todayWeather = MutableLiveData<ForecastItem>()

    private val _dailyForecast = MutableLiveData<List<ForecastItem>>()

    private val _hourlyForecast = MutableLiveData<List<HourForecast>>()

    val location: LiveData<String>
        get() {
            return _location
        }

    val description: LiveData<String> = Transformations.map(_todayWeather) {
        it.mainDescription
    }

    val temperature: LiveData<Double> = Transformations.map(_todayWeather) {
        it.temp
    }

    val weatherId: LiveData<Int> = Transformations.map(_todayWeather) {
        it.weatherId
    }

    val dailyForecast: LiveData<List<ForecastItem>>
        get() {
            return _dailyForecast
        }

    val hourlyForecast: LiveData<List<HourForecast>>
        get() {
            return _hourlyForecast
        }

    private val _isMetric = MutableLiveData<Boolean>().apply { this.value = true }

    val isMetric: LiveData<Boolean>
        get() {
            return _isMetric
        }

    init {
        onLocationChanged(initLocation)
    }

    fun viewSelectedDay(forecastItem: ForecastItem) {
        _viewSelectedDay.value = forecastItem
    }

    fun viewSelectedDayComplete() {
        _viewSelectedDay.value = null
    }

    fun onUnitChanged(isMetric: Boolean) {
        _isMetric.value = isMetric
    }

    private fun getTodayWeather(city: String) {
        viewModelScope.launch {
            try {
                val cityWeather = repository.getTodayForecast(city)
                _todayWeather.value = cityWeather
                _showError.value = false
            } catch (e: Exception) {
                // Show a Toast error message and hide the progress bar.
                _showError.value = true
            }
        }
    }

    private fun getDailyForecast(city: String) {
        viewModelScope.launch {
            try {
                val dailyForecast = repository.getDaysForecast(city)
                _dailyForecast.value = dailyForecast
                _showError.value = false
            } catch (e: Exception) {
                // Show a Toast error message and hide the progress bar.
                _showError.value = true
            }
        }
    }

    fun onLocationChanged(city: String) {
        _location.value = city
        getTodayWeather(city)
        getDailyForecast(city)
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
    class Factory(private val app: Application, private val city: String) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeFragmentViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HomeFragmentViewModel(app, city) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }

}