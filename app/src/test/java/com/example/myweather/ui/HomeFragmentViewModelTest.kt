package com.example.myweather.ui

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.example.myweather.repository.ForecastItem
import com.example.myweather.repository.GeoLocationRepository
import com.example.myweather.repository.WeatherRepository
import com.example.myweather.repository.WorkManagerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.*
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.Spy

class HomeFragmentViewModelTest {
    protected val mainThreadSurrogate = newSingleThreadContext("UI thread")
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
    private var isTrackByLocationPref = false
    private val defaultLocation = "Toronto,CA"
    private var isHourlySyncPref = false
    private val isMetr = true

    @Spy
    val _forecast = MutableLiveData<List<ForecastItem>>()

    @Mock
    lateinit var forecastObserver: Observer<List<ForecastItem>>

    val forecastItem1 = ForecastItem(
        defaultLocation,
        123123123,
        12,
        12.0,
        18.0,
        70,
        101.0,
        12f,
        43f,
        15.0,
        "Raining",
        "Light rain"
    )
    val forecastItem2 = ForecastItem(
        defaultLocation,
        123123123,
        12,
        12.0,
        18.0,
        70,
        101.0,
        12f,
        43f,
        15.0,
        "Raining",
        "Light rain"
    )
    val forecastItem3 = ForecastItem(
        defaultLocation,
        123123123,
        12,
        12.0,
        18.0,
        70,
        101.0,
        12f,
        43f,
        15.0,
        "Raining",
        "Light rain"
    )
    val forecastItem4 = ForecastItem(
        defaultLocation,
        123123123,
        12,
        12.0,
        18.0,
        70,
        101.0,
        12f,
        43f,
        15.0,
        "Raining",
        "Light rain"
    )
    val forecastItem5 = ForecastItem(
        defaultLocation,
        123123123,
        12,
        12.0,
        18.0,
        70,
        101.0,
        12f,
        43f,
        15.0,
        "Raining",
        "Light rain"
    )
    val forecastItem6 = ForecastItem(
        defaultLocation,
        123123123,
        12,
        12.0,
        18.0,
        70,
        101.0,
        12f,
        43f,
        15.0,
        "Raining",
        "Light rain"
    )
    val forecastItem7 = ForecastItem(
        defaultLocation,
        123123123,
        12,
        12.0,
        18.0,
        70,
        101.0,
        12f,
        43f,
        15.0,
        "Raining",
        "Light rain"
    )
    val forecastItem8 = ForecastItem(
        defaultLocation,
        123123123,
        12,
        12.0,
        18.0,
        70,
        101.0,
        12f,
        43f,
        15.0,
        "Raining",
        "Light rain"
    )
    val forecastItem9 = ForecastItem(
        defaultLocation,
        123123123,
        12,
        12.0,
        18.0,
        70,
        101.0,
        12f,
        43f,
        15.0,
        "Raining",
        "Light rain"
    )

    @Before
    fun setup() {
        // helpful when you want to execute a test in situations where the platform Main dispatcher is not available
        // This is to replace Dispatchers.Main with a testing dispatcher.
        Dispatchers.setMain(mainThreadSurrogate)
        MockitoAnnotations.initMocks(this)

    }

    @Test
    fun testTransformDailyForecast() {
        _forecast.value = listOf(
            forecastItem1,
            forecastItem2,
            forecastItem3,
            forecastItem4,
            forecastItem5,
            forecastItem6,
            forecastItem7,
            forecastItem8,
            forecastItem9
        )
        Mockito.`when`(weatherRepository.forecast).thenReturn(_forecast)
        viewModel = HomeFragmentViewModel(
            application,
            weatherRepository,
            geoLocationRepository,
            workManagerRepository,
            isTrackByLocationPref,
            defaultLocation,
            isHourlySyncPref,
            isMetr
        )
        viewModel.hourlyForecast.observeForever(forecastObserver)
        val hourlyForecast = viewModel.hourlyForecast
        Assert.assertEquals(8, hourlyForecast.value!!.size)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        mainThreadSurrogate.close()
    }

}