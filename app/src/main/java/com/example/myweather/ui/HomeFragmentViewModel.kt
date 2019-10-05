package com.example.myweather.ui

import android.app.Application
import androidx.lifecycle.*
import androidx.work.*
import com.example.myweather.database.ForecastItemDatabase
import com.example.myweather.repository.DailyForecastItem
import com.example.myweather.repository.ForecastItem
import com.example.myweather.repository.GeoLocationRepository
import com.example.myweather.repository.WeatherRepository
import com.example.myweather.utils.WeatherUtils
import com.example.myweather.worker.KEY_CITY_SYNC
import com.example.myweather.worker.WeatherSyncWorker
import com.example.myweather.worker.WeatherSyncWorker.Companion.MY_WEATHER_SYNC_BACKGROUND_WORK_NAME
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.*
import timber.log.Timber
import java.util.concurrent.TimeUnit

class HomeFragmentViewModel(application: Application) : AndroidViewModel(application) {
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

    private val weatherRepository = WeatherRepository(ForecastItemDatabase.getInstance(application))
    private val geoLocationRepository = GeoLocationRepository(application.applicationContext)
    private val handler = CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable)
    }

    private var mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            result?.let { locationResult ->
                if (mIsTrackByLocationPref) {
                    viewModelScope.launch(handler) {
                        val address = geoLocationRepository.getAddress(locationResult.lastLocation)
                        getWeatherByCity(address.city + "," + address.country, false)
                    }
                }
            }
        }
    }

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

    val description: LiveData<String> = Transformations.map(weatherRepository.todayForecast) {
        it.mainDescription
    }

    val temperature: LiveData<Double> = Transformations.map(weatherRepository.todayForecast) {
        it.temp
    }

    val weatherId: LiveData<Int> = Transformations.map(weatherRepository.todayForecast) {
        it.weatherId
    }

    val dailyForecast: LiveData<List<DailyForecastItem>> = Transformations.map(weatherRepository.forecast) {
        // get a list of items that are more than 24 hours away from today
        val list = it.subList(NUM_ITEMS_PER_DAY, it.lastIndex)
        // group items within their days
        val dailyForecastMap = WeatherUtils.groupItemsIntoDays(list)
        // calculate average temperature, min and max for each day
        val dailyForecastItems = WeatherUtils.transformToDailyItems(dailyForecastMap)
        dailyForecastItems
    }

    val hourlyForecast: LiveData<List<ForecastItem>> = Transformations.map(weatherRepository.forecast) {
        it.subList(0, NUM_ITEMS_PER_DAY)
    }

    private val _isMetric = MutableLiveData<Boolean>().apply { this.value = true }

    val isMetric: LiveData<Boolean>
        get() {
            return _isMetric
        }

    val windSpeed: LiveData<Float> = Transformations.map(weatherRepository.todayForecast) {
        it.windSpeed
    }

    val degrees: LiveData<Float> = Transformations.map(weatherRepository.todayForecast) {
        it.degrees
    }

    val minTemp: LiveData<Double> = Transformations.map(weatherRepository.todayForecast) {
        it.minTemp
    }

    val maxTemp: LiveData<Double> = Transformations.map(weatherRepository.todayForecast) {
        it.maxTemp
    }

    val pressure: LiveData<Double> = Transformations.map(weatherRepository.todayForecast) {
        it.pressure
    }

    val humidity: LiveData<Int> = Transformations.map(weatherRepository.todayForecast) {
        it.humidity
    }

    private var mIsTrackByLocationPref = false
    private var mIsHourlySyncPref = false

    fun onFragmentResume() {
        if (mIsTrackByLocationPref) {
            onStartTrackingByLocation()
        }
    }

    fun onFragmentPause() {
        if (mIsTrackByLocationPref) {
            onStopTrackingByLocation()
        }
    }

    fun getWeather(isTrackByLocationPref: Boolean, location: String) {
        if (isTrackByLocationPref == mIsTrackByLocationPref) {
            return
        }

        mIsTrackByLocationPref = isTrackByLocationPref
        if (isTrackByLocationPref) {
            onStartTrackingByLocation()
        } else {
            onStopTrackingByLocation()
            getWeatherByCity(location, false)
        }
    }

    fun getWeatherByCity(city: String, isForcedRefresh: Boolean) {
        _location.value = city
        getTodayWeather(city, isForcedRefresh)
        getDailyForecast(city, isForcedRefresh)
    }

    fun refresh() {
        _location.value?.let {
            getWeatherByCity(it, true)
        }
    }

    fun onHourlySyncPrefChange(isHourlySync: Boolean) {
        if (mIsHourlySyncPref == isHourlySync) {
            return
        }

        mIsHourlySyncPref = isHourlySync
        if (isHourlySync) setupHourlySync() else cancelHourlySync()
    }

    fun cancelHourlySync() {
        workManager.cancelUniqueWork(MY_WEATHER_SYNC_BACKGROUND_WORK_NAME)
    }

    fun setupHourlySync() {
        val data = workDataOf(
            KEY_CITY_SYNC to _location.value
        )
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.UNMETERED)
            .build()

        val workRequest =
            PeriodicWorkRequestBuilder<WeatherSyncWorker>(60, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .setInputData(data).build()
        workManager.enqueueUniquePeriodicWork(
            MY_WEATHER_SYNC_BACKGROUND_WORK_NAME,
            ExistingPeriodicWorkPolicy.REPLACE,
            workRequest
        )
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

    private fun onStartTrackingByLocation() {
        geoLocationRepository.startTrackingByLocation(mLocationCallback)
    }

    private fun onStopTrackingByLocation() {
        geoLocationRepository.stopTrackingByLocation(mLocationCallback)
    }

    private fun getTodayWeather(city: String, isForcedRefresh: Boolean) {
        viewModelScope.launch {
            try {
                weatherRepository.getCurrentForecast(city, isForcedRefresh)
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
                weatherRepository.getComingDaysForecast(city, isForcedRefresh)
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
     * Factory for constructing HomeFragmentViewModel with parameter
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