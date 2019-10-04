package com.example.myweather.ui

import android.Manifest
import android.app.SearchManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.text.TextUtils
import android.view.*
import android.widget.SearchView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myweather.R
import com.example.myweather.databinding.FragmentHomeBinding
import com.google.android.gms.location.*
import kotlinx.coroutines.*
import timber.log.Timber
import java.io.IOException
import java.util.*
import kotlin.coroutines.CoroutineContext

class HomeFragment : Fragment(), CoroutineScope, SharedPreferences.OnSharedPreferenceChangeListener {
    companion object {
        const val REQUEST_LOCATION_PERMISSION = 1
    }

    private val job = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + job
    private val handler = CoroutineExceptionHandler { coroutineContext, throwable ->
        Timber.e(throwable)
    }

    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeFragmentViewModel by lazy {
        ViewModelProviders.of(this, HomeFragmentViewModel.Factory(requireActivity().application))
            .get(HomeFragmentViewModel::class.java)
    }
    private lateinit var mSearchView: SearchView
    private lateinit var mMenuItem: MenuItem
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    private var mTrackingLocation = false

    private var mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult?) {
            if (mTrackingLocation) {
                result?.let {
                    launch(handler) {
                        val deferred = async(Dispatchers.Default) {
                            getLocation(it.lastLocation)
                        }
                        Toast.makeText(activity, "address=" + deferred.await(), Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }
        }
    }

    fun getLocation(location: Location): String {
        val geoCoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses = geoCoder.getFromLocation(location.latitude, location.longitude, 1)
        if (addresses == null || addresses.size == 0) {
            throw IOException("Address not found")
        }

        val address = addresses.get(0)
        val addressParts = mutableListOf<String>()
        for (i in 0..address.maxAddressLineIndex) {
            addressParts.add(address.getAddressLine(i))
        }

        return TextUtils.join(",", addressParts)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        val adapter = DailyForecastAdapter(context!!, viewModel.isMetric.value ?: true, ForecastClickListener { day ->
            viewModel.viewSelectedDay(day)
        })

        binding.content.dailyForecast.adapter = adapter

        val hourlyAdapter = HourlyForecastAdapter(context!!, viewModel.isMetric.value ?: true)
        binding.content.hourlyForecast.adapter = hourlyAdapter
        binding.content.hourlyForecast.layoutManager =
            LinearLayoutManager(context!!, LinearLayoutManager.HORIZONTAL, false)

        viewModel.isMetric.observe(viewLifecycleOwner, Observer { isMetric ->
            adapter.mIsMetric = isMetric ?: true
        })

        viewModel.showError.observe(viewLifecycleOwner, Observer { showNetworkError ->
            if (showNetworkError) {
                Toast.makeText(activity, "Network Error", Toast.LENGTH_LONG).show()
                viewModel.onNetworkErrorShown()
            }
        })

        viewModel.viewSelectedDay.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                this.findNavController().navigate(HomeFragmentDirections.actionHomeToDayDetails(it))
                viewModel.viewSelectedDayComplete()
            }
        })

        PreferenceManager.getDefaultSharedPreferences(activity)
            .registerOnSharedPreferenceChangeListener(this)
        setHasOptionsMenu(true)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity /* Activity context */)
        val hourlySync = sharedPreferences.getBoolean(resources.getString(R.string.pref_hourly_sync_key), false)
        if (hourlySync) viewModel.setupHourlySync()

        binding.swipeRefresh.setOnRefreshListener {
            refresh()
        }

        val isTrackLocationEnable =
            sharedPreferences.getBoolean(resources.getString(R.string.pref_enable_geo_location_key), false)
        if (isTrackLocationEnable) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this.requireActivity())
            startTrackingLocation()
        } else {
            val location = sharedPreferences.getString(
                resources.getString(R.string.pref_location_key),
                resources.getString(R.string.pref_location_default)
            )

            location?.let {
                viewModel.getWeatherForLocation(location, false)
            }
        }

        return binding.root
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, pref: String) {
        when (pref) {
            resources.getString(R.string.pref_location_key) -> viewModel.getWeatherForLocation(
                sharedPreferences.getString(
                    pref,
                    resources.getString(R.string.pref_location_default)
                )!!, false
            )
            resources.getString(R.string.pref_units_key) -> {
                val isMetric = sharedPreferences.getString(pref, "") == resources.getString(R.string.pref_units_metric)
                viewModel.onUnitChanged(isMetric)
            }
            resources.getString(R.string.pref_hourly_sync_key) -> {
                if (sharedPreferences.getBoolean(
                        pref,
                        false
                    )
                ) viewModel.setupHourlySync() else viewModel.cancelHourlySync()
            }
            resources.getString(R.string.pref_enable_geo_location_key) -> {
                mTrackingLocation =
                    sharedPreferences.getBoolean(pref, false)
            }
        }
    }

    private fun startTrackingLocation() {
        if (ActivityCompat.checkSelfPermission(
                this.requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_PERMISSION
            )
        } else {
            mTrackingLocation = true
            mFusedLocationClient.requestLocationUpdates(
                getLocationRequest(), mLocationCallback,
                null /* Looper */
            )
        }

    }

    private fun getLocationRequest(): LocationRequest {
        val locationRequest = LocationRequest()
        locationRequest.interval = 100000
        locationRequest.fastestInterval = 50000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        return locationRequest
    }

    override fun onDestroy() {
        super.onDestroy()
        PreferenceManager.getDefaultSharedPreferences(activity /* Activity context */)
            .unregisterOnSharedPreferenceChangeListener(this)
    }

    private fun refresh() {
        binding.swipeRefresh.isRefreshing = true
        viewModel.refresh()
        binding.swipeRefresh.isRefreshing = false
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu, menu)
        val searchManager = activity?.getSystemService(Context.SEARCH_SERVICE) as SearchManager
        mMenuItem = menu.findItem(R.id.search)
        mSearchView = (mMenuItem.actionView as SearchView).apply {
            // Assumes current activity is the searchable activity
            setSearchableInfo(searchManager.getSearchableInfo(activity?.componentName))
            isIconifiedByDefault = true
        }

        mSearchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(value: String?): Boolean {
                    value?.let {
                        viewModel.getWeatherForLocation(it, false)
                    }

                    return true
                }

                override fun onQueryTextChange(value: String?): Boolean {
                    return true
                }
            }
        )

        mMenuItem.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(p0: MenuItem?): Boolean {
                return true
            }

            override fun onMenuItemActionCollapse(p0: MenuItem?): Boolean {
                return true
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.refresh) {
            refresh()
            return true
        }

        return NavigationUI.onNavDestinationSelected(
            item,
            view!!.findNavController()
        ) || super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                // If the permission is granted, get the location,
                // otherwise, show a Toast
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mTrackingLocation = true
                    startTrackingLocation()
                } else {
                    mTrackingLocation = false
                    // TODO - set geo location preference to false
                    Toast.makeText(this.requireContext(), R.string.location_permission_denied, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        coroutineContext.cancelChildren()
    }
}
