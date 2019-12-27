package com.example.myweather.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.example.myweather.repository.*
import com.example.myweather.utils.WeatherUtils
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.*
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject

class HomeFragmentViewModel @Inject constructor(
    application: Application,
    private val weatherRepository: WeatherRepository,
    private val geoLocationRepository: GeoLocationRepository,
    private val workManagerRepository: WorkManagerRepository,
    private val sharedPreferencesRepository: SharedPreferencesRepository
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
     * This is the main scope for all coroutines launched by ViewModel.
     *
     * Since we pass viewModelJob, you can cancel all coroutines launched by uiScope by calling
     * viewModelJob.cancel()
     */
    private val viewModelScope = CoroutineScope(viewModelJob + Dispatchers.Main)

    private val handler = CoroutineExceptionHandler { _, throwable ->
        Timber.e(throwable)
    }

    private var mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            result?.let { locationResult ->
                if (sharedPreferencesRepository.isLocationTrackingEnabled()) {
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
    private var _showError = MutableLiveData<ErrorType>()
    /**
     * Event triggered for network error. Views should use this to get access
     * to the data.
     */
    val showError: LiveData<ErrorType>
        get() = _showError


    private val _viewSelectedDay = MutableLiveData<DailyForecastItem>()

    val viewSelectedDay: LiveData<DailyForecastItem>
        get() = _viewSelectedDay

    private val _location = MutableLiveData<String>().apply {
        sharedPreferencesRepository.getDefaultLocation()
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

    val dailyForecast: LiveData<List<DailyForecastItem>> =
        Transformations.map(weatherRepository.forecast) {
            // group items within their days discard the items that are within today
            val dailyForecastMap = WeatherUtils.groupItemsIntoDays(it)
            // calculate average temperature, min and max for each day
            val dailyForecastItems = WeatherUtils.transformToDailyItems(dailyForecastMap)
            dailyForecastItems
        }

    // get the forcast items for the next 24 hours
    val hourlyForecast: LiveData<List<ForecastItem>> =
        Transformations.map(weatherRepository.forecast) {
            it.subList(0, NUM_ITEMS_PER_DAY)
        }

    private val _isMetric =
        MutableLiveData<Boolean>().apply { this.value = sharedPreferencesRepository.isMetricUnit() }

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

    private var _processing = MutableLiveData<Int>()

    val processing: LiveData<Int>
        get() {
            return _processing
        }

    init {
        if (!sharedPreferencesRepository.isLocationDBPopulated()) {
            workManagerRepository.populateLocationDb()
        }

        if (sharedPreferencesRepository.isLocationTrackingEnabled()) {
            onStartTrackingByLocation()
        } else {
            getWeatherByLocation(sharedPreferencesRepository.getDefaultLocation(), false)
        }
    }

    fun onFragmentResume() {
        if (sharedPreferencesRepository.isLocationTrackingEnabled()) {
            onStartTrackingByLocation()
        }
    }

    fun onFragmentPause() {
        if (sharedPreferencesRepository.isLocationTrackingEnabled()) {
            onStopTrackingByLocation()
        }
    }

    private fun onLocationChange(location: String) {
        onStopTrackingByLocation()
        getWeatherByLocation(location, false)
    }

    fun onLocationTrackingPreferenceChange() {
        if (sharedPreferencesRepository.isLocationTrackingEnabled()) {
            onStartTrackingByLocation()
        } else {
            onLocationChange(sharedPreferencesRepository.getDefaultLocation())
        }
    }

    fun getWeatherByLocation(location: String, isForcedRefresh: Boolean) {
        val handler = CoroutineExceptionHandler { _, throwable ->
            _processing.value = 100
            if (throwable is HttpException && throwable.code() == 404) _showError.value =
                ErrorType.LocationNotFound(location) else _showError.value =
                ErrorType.GenericError()
            Timber.e("Repository exception %s", throwable)
        }

        viewModelScope.launch(handler) {
            _processing.value = 0
            val currentForecast =
                async { weatherRepository.getCurrentForecast(location, isForcedRefresh) }
            _processing.value = 20
            val comingDaysForecast =
                async { weatherRepository.getComingDaysForecast(location, isForcedRefresh) }
            _processing.value = 40
            currentForecast.await()
            _processing.value = 50
            comingDaysForecast.await()
            _processing.value = 100
            _location.value = location
        }
    }

    fun refresh() {
        _location.value?.let {
            getWeatherByLocation(it, true)
        }
    }

    fun onHourlySyncPreferenceChange(isHourlySync: Boolean) {
        if (sharedPreferencesRepository.isHourlySyncEnabled() == isHourlySync) {
            return
        }

        if (isHourlySync) workManagerRepository.enableHourlySync() else workManagerRepository.cancelHourlySync()
    }

    fun viewSelectedDay(day: DailyForecastItem) {
        _viewSelectedDay.value = day
    }

    fun viewSelectedDayCompleted() {
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

    /**
     * Resets the network error flag.
     */
    fun onNetworkErrorShown() {
        _showError.value = null
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }

}

sealed class ErrorType {
    class GenericError : ErrorType()
    class LocationNotFound(val location: String) : ErrorType()
}
