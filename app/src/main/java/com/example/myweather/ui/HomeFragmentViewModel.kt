package com.example.myweather.ui

import android.app.Application
import androidx.lifecycle.*
import com.example.myweather.database.ForecastItemDatabase
import com.example.myweather.repository.*
import com.example.myweather.utils.WeatherUtils
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.*
import timber.log.Timber

class HomeFragmentViewModel(
    application: Application,
    private var mIsTrackByLocationPref: Boolean,
    val defaultLocation: String,
    private var mIsHourlySyncPref: Boolean
) : AndroidViewModel(application) {
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

    /**
     * This is the main scope for all coroutines launched by MainViewModel.
     *
     * Since we pass viewModelJob, you can cancel all coroutines launched by uiScope by calling
     * viewModelJob.cancel()
     */
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val weatherRepository: WeatherRepositoryInterface =
        WeatherRepository(ForecastItemDatabase.getInstance(application))
    private val geoLocationRepository: GeoLocationRepositoryInterface by lazy {
        GeoLocationRepository(application.applicationContext)
    }
    private val workManagerRepository: WorkManagerRepositoryInterface by lazy {
        WorkManagerRepository(application)
    }
    private val handler = CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable)
    }

    private var mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            result?.let { locationResult ->
                if (mIsTrackByLocationPref) {
                    viewModelScope.launch(handler) {
                        val address = geoLocationRepository.getAddress(locationResult.lastLocation)
                        getWeatherByLocation(address.city + "," + address.country, false)
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

    private val _location = MutableLiveData<String>().apply {
        defaultLocation
    }

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

    init {
        if (mIsTrackByLocationPref) {
            onStartTrackingByLocation()
        } else {
            getWeatherByLocation(defaultLocation, false)
        }
    }

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

    fun onLocationChange(location: String) {
        onStopTrackingByLocation()
        getWeatherByLocation(location, false)
    }

    fun onLocationTrackingPreferenceChange(isTrackByLocationPref: Boolean, location: String) {
        if (isTrackByLocationPref == mIsTrackByLocationPref) {
            return
        }

        mIsTrackByLocationPref = isTrackByLocationPref
        if (isTrackByLocationPref) {
            onStartTrackingByLocation()
        } else {
            onLocationChange(location)
        }
    }

    fun getWeatherByLocation(city: String, isForcedRefresh: Boolean) {
        _location.value = city
        getTodayWeather(city, isForcedRefresh)
        getDailyForecast(city, isForcedRefresh)
    }

    fun refresh() {
        _location.value?.let {
            getWeatherByLocation(it, true)
        }
    }

    fun onHourlySyncPreferenceChange(isHourlySync: Boolean) {
        if (mIsHourlySyncPref == isHourlySync) {
            return
        }

        mIsHourlySyncPref = isHourlySync
        if (isHourlySync) workManagerRepository.enableHourlySync() else workManagerRepository.cancelHourlySync()
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
        geoLocationRepository.locationTracking(mLocationCallback)
    }

    private fun onStopTrackingByLocation() {
        geoLocationRepository.stopLocationTracking(mLocationCallback)
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
    class Factory(
        val app: Application,
        val isTrackByLocationPref: Boolean,
        val defaultLocation: String,
        val isHourlySyncPref: Boolean
    ) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeFragmentViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return HomeFragmentViewModel(app, isTrackByLocationPref, defaultLocation, isHourlySyncPref) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }

}