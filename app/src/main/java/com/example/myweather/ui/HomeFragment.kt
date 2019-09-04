package com.example.myweather.ui

import android.content.SharedPreferences
import android.os.Bundle
import android.view.*
import android.widget.Toast
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

class HomeFragment : Fragment(), SharedPreferences.OnSharedPreferenceChangeListener {
    private lateinit var binding: FragmentHomeBinding
    private val viewModel: HomeFragmentViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity /* Activity context */)
        val location = sharedPreferences.getString(
            resources.getString(R.string.pref_location_key),
            resources.getString(R.string.pref_location_default)
        )
        ViewModelProviders.of(this, HomeFragmentViewModel.Factory(activity.application, location!!))
            .get(HomeFragmentViewModel::class.java)
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

        return binding.root
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, pref: String) {
        if (pref == resources.getString(R.string.pref_location_key)) {
            viewModel.onLocationChange(
                sharedPreferences.getString(
                    pref,
                    resources.getString(R.string.pref_location_default)
                )!!, false
            )
        } else if (pref == resources.getString(R.string.pref_units_key)) {
            val isMetric = sharedPreferences.getString(pref, "") == resources.getString(R.string.pref_units_metric)
            viewModel.onUnitChanged(isMetric)
        } else if (pref == resources.getString(R.string.pref_hourly_sync_key)) {
            if (sharedPreferences.getBoolean(pref, false)) viewModel.setupHourlySync() else viewModel.cancelHourlySync()

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
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu, menu)
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
}
