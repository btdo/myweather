package com.example.myweather.ui

import androidx.lifecycle.*
import com.example.myweather.repository.*
import com.example.myweather.utils.WeatherUtils
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import retrofit2.HttpException
import timber.log.Timber
import javax.inject.Inject


@ExperimentalCoroutinesApi
class HomeFragmentViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val geoLocationRepository: GeoLocationRepository,
    private val workManagerRepository: WorkManagerRepository,
    private val sharedPreferencesRepository: SharedPreferencesRepository
) : ViewModel() {
    companion object {
        // backend returns in 3 hour internal, so 8x3= 24 for the upcoming day
        const val NUM_ITEMS_PER_DAY = 8
    }

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
            Timber.e(throwable, "Repository exception")
        }

        viewModelScope.launch(handler) {
            _location.value = location
            var progress = 0
            _processing.value = progress
            launch {
                weatherRepository.getTodayForecast(location, isForcedRefresh)
                progress += 50
                _processing.value = progress
            }

            launch {
                weatherRepository.getComingDaysForecast(location, isForcedRefresh)
                progress += 50
                _processing.value = progress
            }
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

}

sealed class ErrorType {
    class GenericError : ErrorType()
    class LocationNotFound(val location: String) : ErrorType()
}
