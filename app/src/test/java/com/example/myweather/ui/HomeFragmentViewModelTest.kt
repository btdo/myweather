package com.example.myweather.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.example.myweather.MainCoroutineRule
import com.example.myweather.getOrAwaitValue
import com.example.myweather.repository.*
import com.example.myweather.utils.generateListOfForecastItems
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.Spy

@ExperimentalCoroutinesApi
class HomeFragmentViewModelTest {

    @Rule
    @JvmField
    var mainCoroutineRule = MainCoroutineRule()

    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: HomeFragmentViewModel
    @Mock
    private lateinit var weatherRepository: WeatherRepository
    @Mock
    private lateinit var geoLocationRepository: GeoLocationRepository
    @Mock
    private lateinit var workManagerRepository: WorkManagerRepository
    @Mock
    private lateinit var sharedPreferencesRepository: SharedPreferencesRepository

    private var isTrackByLocationPref = false
    private val defaultLocation = "Toronto,CA"
    private var isHourlySyncPref = false
    private val isMetr = true

    @Spy
    val _forecast = MutableLiveData<List<ForecastItem>>()

    @Before
    fun setup() {
        MockitoAnnotations.initMocks(this)
        Mockito.`when`(sharedPreferencesRepository.isLocationTrackingEnabled())
            .thenReturn(isTrackByLocationPref)
        Mockito.`when`(sharedPreferencesRepository.getDefaultLocation()).thenReturn(defaultLocation)
        Mockito.`when`(sharedPreferencesRepository.isHourlySyncEnabled())
            .thenReturn(isHourlySyncPref)
        Mockito.`when`(sharedPreferencesRepository.isMetricUnit()).thenReturn(isMetr)
    }

    @Test
    fun testTransformHourlyForecast() {
        _forecast.value = generateListOfForecastItems("Toronto, CA")
        Mockito.`when`(weatherRepository.forecast).thenReturn(_forecast)

        viewModel = HomeFragmentViewModel(
            weatherRepository,
            geoLocationRepository,
            workManagerRepository,
            sharedPreferencesRepository
        )

        val hourlyForecast = viewModel.hourlyForecast.getOrAwaitValue()
        Assert.assertEquals(8, hourlyForecast.size)
    }

    @Test
    fun testTransformDailyForecast() {
        _forecast.value = generateListOfForecastItems("Toronto, CA")
        Mockito.`when`(weatherRepository.forecast).thenReturn(_forecast)
        viewModel = HomeFragmentViewModel(
            weatherRepository,
            geoLocationRepository,
            workManagerRepository,
            sharedPreferencesRepository
        )

        val dailyForecast = viewModel.dailyForecast.getOrAwaitValue()
        Assert.assertEquals(5, dailyForecast.size)
    }

    @Test
    fun testGetWeatherByLocation() {

    }
}