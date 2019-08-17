package com.example.myweather.ui

import android.app.Application
import androidx.lifecycle.*
import com.example.myweather.database.ForecastItemDatabase
import com.example.myweather.repository.DailyForecastItem
import com.example.myweather.repository.ForecastItem
import com.example.myweather.repository.WeatherRepository
import com.example.myweather.utils.WeatherUtils
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

    private val _viewSelectedDay = MutableLiveData<DailyForecastItem>()

    val viewSelectedDay: LiveData<DailyForecastItem>
        get() = _viewSelectedDay

    private val _location = MutableLiveData<String>()
    val location: LiveData<String>
        get() {
            return _location
        }


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

    private val repository = WeatherRepository(ForecastItemDatabase.getInstance(application))

    val description: LiveData<String> = Transformations.map(repository.todayForecast) {
        it.mainDescription
    }

    val temperature: LiveData<Double> = Transformations.map(repository.todayForecast) {
        it.temp
    }

    val weatherId: LiveData<Int> = Transformations.map(repository.todayForecast) {
        it.weatherId
    }

    val dailyForecast: LiveData<List<DailyForecastItem>> = Transformations.map(repository.dailyForecast) {
        val list = it.subList(8, it.lastIndex)
        val dailyForecastMap = WeatherUtils.groupItemsIntoDays(list)
        val dailyForecastItems = WeatherUtils.transformToDailyItems(dailyForecastMap)
        dailyForecastItems
    }

    val hourlyForecast: LiveData<List<ForecastItem>> = Transformations.map(repository.dailyForecast) {
        it.subList(0, 8)
    }

    private val _isMetric = MutableLiveData<Boolean>().apply { this.value = true }

    val isMetric: LiveData<Boolean>
        get() {
            return _isMetric
        }

    init {
        onLocation(initLocation, false)
    }

    fun viewSelectedDay(day: DailyForecastItem) {
        _viewSelectedDay.value = day
    }

    fun viewSelectedDayComplete() {
        _viewSelectedDay.value = null
    }

    fun onUnitChanged(isMetric: Boolean) {
        _isMetric.value = isMetric
    }

    private fun getTodayWeather(city: String, isForcedRefresh: Boolean) {
        viewModelScope.launch {
            try {
                repository.getCurrentForecast(city, isForcedRefresh)
                _showError.value = false
            } catch (e: Exception) {
                // Show a Toast error message and hide the progress bar.
                _showError.value = true
            }
        }
    }

    private fun getDailyForecast(city: String, isForcedRefresh: Boolean) {
        viewModelScope.launch {
            try {
                repository.getDaysForecast(city, isForcedRefresh)
                _showError.value = false
            } catch (e: Exception) {
                // Show a Toast error message and hide the progress bar.
                _showError.value = true
            }
        }
    }

    fun onLocation(city: String, isForcedRefresh: Boolean) {
        _location.value = city
        getTodayWeather(city, isForcedRefresh)
        getDailyForecast(city, isForcedRefresh)
    }

    fun refresh() {
        _location.value?.let {
            onLocation(it, true)
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