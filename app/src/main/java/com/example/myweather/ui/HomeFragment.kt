package com.example.myweather.ui

import android.Manifest
import android.app.Activity
import android.app.SearchManager
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myweather.MyWeatherApplication
import com.example.myweather.R
import com.example.myweather.databinding.FragmentHomeBinding
import com.example.myweather.repository.SharedPreferencesRepository
import com.example.myweather.repository.WeatherCondition
import com.example.myweather.utils.WeatherUtils
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext


class HomeFragment : Fragment(), CoroutineScope,
    SharedPreferences.OnSharedPreferenceChangeListener {
    companion object {
        const val REQUEST_LOCATION_PERMISSION = 1
    }

    private val mJob = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main + mJob

    @Inject
    lateinit var sharedPreferencesRepository: SharedPreferencesRepository

    @Inject
    lateinit var viewModeFactory: ViewModelProvider.Factory

    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: HomeFragmentViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity().application as MyWeatherApplication).appComponent.inject(this)
    }

    private lateinit var mSearchView: SearchView
    private lateinit var mMenuItem: MenuItem


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater)
        binding.lifecycleOwner = this
        viewModel =
            ViewModelProviders.of(this, this.viewModeFactory).get(HomeFragmentViewModel::class.java)
        binding.viewModel = viewModel

        val adapter =
            DailyForecastAdapter(
                requireContext(),
                viewModel.isMetric.value ?: true,
                ForecastClickListener { day ->
                    viewModel.viewSelectedDay(day)
                })
        binding.content.dailyForecast.adapter = adapter

        val hourlyAdapter =
            HourlyForecastAdapter(requireContext(), viewModel.isMetric.value ?: true)
        binding.content.hourlyForecast.adapter = hourlyAdapter
        binding.content.hourlyForecast.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        viewModel.isMetric.observe(viewLifecycleOwner, Observer { isMetric ->
            adapter.mIsMetric = isMetric ?: true
        })

        viewModel.showError.observe(viewLifecycleOwner, Observer { showError ->
            showError?.let {
                when (showError) {
                    is ErrorType.GenericError -> {
                        Toast.makeText(activity, "Network Error", Toast.LENGTH_LONG).show()
                    }
                    is ErrorType.LocationNotFound -> {
                        Toast.makeText(
                            activity,
                            "Location '${showError.location}' is not found. Please try again with country code, for example 'Toronto, CA' ",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                viewModel.onNetworkErrorShown()
            }

        })

        viewModel.viewSelectedDay.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                this.findNavController().navigate(HomeFragmentDirections.actionHomeToDayDetails(it))
                viewModel.viewSelectedDayCompleted()
            }
        })

        viewModel.weatherId.observe(viewLifecycleOwner, Observer {
            val animationSettings = WeatherCondition.valueOf(it).animationSettings
            animationSettings?.let {
                animateBackground(animationSettings.drawableId, animationSettings.volume)
            }
        })

        viewModel.processing.observe(viewLifecycleOwner, Observer {
            it?.let { isProcessing ->
                binding.content.pbLoading.visibility =
                    if (isProcessing) View.VISIBLE else View.INVISIBLE
            }
        })

        PreferenceManager.getDefaultSharedPreferences(activity)
            .registerOnSharedPreferenceChangeListener(this)
        setHasOptionsMenu(true)

        binding.swipeRefresh.setOnRefreshListener {
            refresh()
        }

        return binding.root
    }

    private fun animateBackground(@DrawableRes drawableId: Int, volume: Int) {
        launch {
            for (i in 1..volume) {
                WeatherUtils.showerAnimation(
                    requireContext(),
                    binding.content.parent as ViewGroup,
                    drawableId
                )
            }
        }
    }

    private fun onLocationPreferenceChange() {
        viewModel.onLocationTrackingPreferenceChange()
    }


    private fun onHourlySyncPreferenceChange() {
        val sharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(activity /* Activity context */)
        val hourlySync =
            sharedPreferences.getBoolean(resources.getString(R.string.pref_hourly_sync_key), false)
        viewModel.onHourlySyncPreferenceChange(hourlySync)
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, pref: String) {
        when (pref) {
            resources.getString(R.string.pref_location_key) -> onLocationPreferenceChange()
            resources.getString(R.string.pref_units_key) -> {
                val isMetric = sharedPreferences.getString(
                    pref,
                    ""
                ) == resources.getString(R.string.pref_units_metric)
                viewModel.onUnitChanged(isMetric)
            }
            resources.getString(R.string.pref_hourly_sync_key) -> onHourlySyncPreferenceChange()
            resources.getString(R.string.pref_enable_geo_location_key) -> {
                if (sharedPreferences.getBoolean(
                        pref,
                        false
                    )
                ) startTrackingLocation() else stopTrackingLocation()
            }
        }
    }

    private fun stopTrackingLocation() {
        onLocationPreferenceChange()
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
            onLocationPreferenceChange()
        }
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
        val searchManager =
            requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        mMenuItem = menu.findItem(R.id.search)
        mSearchView = (mMenuItem.actionView as SearchView).apply {
            // Assumes current activity is the searchable activity
            setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
            isIconifiedByDefault = true
        }

        mSearchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(value: String?): Boolean {
                    value?.let {
                        viewModel.getWeatherByLocation(it, false)
                        hideKeyboardFrom(requireContext(), mSearchView)
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_LOCATION_PERMISSION -> {
                // If the permission is granted, get the location,
                // otherwise, show a Toast
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startTrackingLocation()
                } else {
                    sharedPreferencesRepository.setLocationTracking(false)
                    Toast.makeText(
                        this.requireContext(),
                        R.string.location_permission_denied,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun hideKeyboardFrom(context: Context, view: View) {
        val imm: InputMethodManager =
            context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun onResume() {
        super.onResume()
        viewModel.onFragmentResume()
    }

    override fun onPause() {
        super.onPause()
        viewModel.onFragmentPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        coroutineContext.cancelChildren()
    }
}
