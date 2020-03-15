package com.example.myweather.ui

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.example.myweather.getOrAwaitValue
import com.example.myweather.repository.*
import com.example.myweather.utils.generateListOfForecastItems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.*
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.Spy

@ExperimentalCoroutinesApi
class HomeFragmentViewModelTest {

    private val testDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher()

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
        Dispatchers.setMain(testDispatcher)
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

    @After
    fun tearDown() {
        Dispatchers.resetMain() // reset main dispatcher to the original Main dispatcher
        testDispatcher.cleanupTestCoroutines()
    }

}