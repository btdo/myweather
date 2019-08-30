package com.example.myweather.ui

import android.app.Application
import androidx.lifecycle.*
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.myweather.database.ForecastItemDatabase
import com.example.myweather.repository.DailyForecastItem
import com.example.myweather.repository.ForecastItem
import com.example.myweather.repository.WeatherRepository
import com.example.myweather.utils.WeatherUtils
import com.example.myweather.worker.KEY_CITY_SYNC
import com.example.myweather.worker.WeatherSyncWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import timber.log.Timber

class HomeFragmentViewModel(application: Application, initLocation: String) : AndroidViewModel(application) {
    companion object {
        // backend returns in 3 hour internal, so 8x3= 24 for the upcoming day
        const val NUM_ITEMS_PER_DAY = 8
    }

    /**
     * This is the job for all coroutines started by this ViewModel.
     *
     * Cancelling this job will cancel all coroutines started by this ViewModel.
     */
    private val viewModelJob = SupervisorJob()

    private val workManager = WorkManager.getInstance(application)

    /**
     * This is the main scope for all coroutines launched by MainViewModel.
     *
     * Since we pass viewModelJob, you can cancel all coroutines launched by uiScope by calling
     * viewModelJob.cancel()
     */
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val repository = WeatherRepository(ForecastItemDatabase.getInstance(application))

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

    private val _location = MutableLiveData<String>().apply {
        this.value = initLocation
    }
    val location: LiveData<String>
        get() {
            return _location
        }

    val description: LiveData<String> = Transformations.map(repository.todayForecast) {
        it.mainDescription
    }

    val temperature: LiveData<Double> = Transformations.map(repository.todayForecast) {
        it.temp
    }

    val weatherId: LiveData<Int> = Transformations.map(repository.todayForecast) {
        it.weatherId
    }

    val dailyForecast: LiveData<List<DailyForecastItem>> = Transformations.map(repository.forecast) {
        // get a list of items that are more than 24 hours away from today
        val list = it.subList(NUM_ITEMS_PER_DAY, it.lastIndex)
        // group items within their days
        val dailyForecastMap = WeatherUtils.groupItemsIntoDays(list)
        // calculate average temperature, min and max for each day
        val dailyForecastItems = WeatherUtils.transformToDailyItems(dailyForecastMap)
        dailyForecastItems
    }

    val hourlyForecast: LiveData<List<ForecastItem>> = Transformations.map(repository.forecast) {
        it.subList(0, NUM_ITEMS_PER_DAY)
    }

    private val _isMetric = MutableLiveData<Boolean>().apply { this.value = true }

    val isMetric: LiveData<Boolean>
        get() {
            return _isMetric
        }

    val windSpeed: LiveData<Float> = Transformations.map(repository.todayForecast) {
        it.windSpeed
    }

    val degrees: LiveData<Float> = Transformations.map(repository.todayForecast) {
        it.degrees
    }

    val minTemp: LiveData<Double> = Transformations.map(repository.todayForecast) {
        it.minTemp
    }

    val maxTemp: LiveData<Double> = Transformations.map(repository.todayForecast) {
        it.maxTemp
    }

    val pressure: LiveData<Double> = Transformations.map(repository.todayForecast) {
        it.pressure
    }

    val humidity: LiveData<Int> = Transformations.map(repository.todayForecast) {
        it.humidity
    }

    init {
        clearCache()
        setupBackgroundTask(initLocation)
        onLocationWeather(initLocation, false)
    }

    fun onLocationWeather(city: String, isForcedRefresh: Boolean) {
        _location.value = city
        getTodayWeather(city, isForcedRefresh)
        getDailyForecast(city, isForcedRefresh)
    }

    fun refresh() {
        _location.value?.let {
            onLocationWeather(it, false)
        }
    }

    fun setupBackgroundTask(city: String) {
        val data = workDataOf(
            KEY_CITY_SYNC to city
        )
        val workRequest = OneTimeWorkRequestBuilder<WeatherSyncWorker>().setInputData(data).build()
        workManager.enqueue(workRequest)
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

    private fun clearCache() {
        viewModelScope.launch {
            try {
                repository.clearCache()
            } catch (e: Exception) {
                Timber.e(e)
            }
        }
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
                repository.getComingDaysForecast(city, isForcedRefresh)
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