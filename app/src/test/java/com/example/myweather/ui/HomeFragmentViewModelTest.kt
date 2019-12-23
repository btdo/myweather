package com.example.myweather.ui

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.myweather.repository.*
import com.example.myweather.utils.generateListOfForecastItems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.*
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import org.mockito.Spy

@ExperimentalCoroutinesApi
class HomeFragmentViewModelTest {
    private val mainThreadSurrogate = newSingleThreadContext("UI thread")
    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var viewModel: HomeFragmentViewModel
    @Mock
    private lateinit var application: Application
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
        // helpful when you want to execute a test in situations where the platform Main dispatcher is not available
        // This is to replace Dispatchers.Main with a testing dispatcher.
        Dispatchers.setMain(mainThreadSurrogate)
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
            application,
            weatherRepository,
            geoLocationRepository,
            workManagerRepository,
            sharedPreferencesRepository
        )

        var forecastObserver = mock(Observer::class.java) as Observer<List<ForecastItem>>
        viewModel.hourlyForecast.observeForever(forecastObserver)
        val hourlyForecast = viewModel.hourlyForecast
        Assert.assertEquals(8, hourlyForecast.value!!.size)
    }

    @Test
    fun testTransformDailyForecast() {
        _forecast.value = generateListOfForecastItems("Toronto, CA")
        Mockito.`when`(weatherRepository.forecast).thenReturn(_forecast)
        viewModel = HomeFragmentViewModel(
            application,
            weatherRepository,
            geoLocationRepository,
            workManagerRepository,
            sharedPreferencesRepository
        )

        var forecastObserver = mock(Observer::class.java) as Observer<List<DailyForecastItem>>
        viewModel.dailyForecast.observeForever(forecastObserver)
        val dailyForecast = viewModel.dailyForecast
        Assert.assertEquals(5, dailyForecast.value!!.size)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
    }

}