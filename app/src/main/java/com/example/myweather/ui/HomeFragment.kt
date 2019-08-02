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
        val binding = FragmentHomeBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        val adapter = DailyForecastAdapter(context!!, viewModel.isMetric.value ?: true, ForecastClickListener { day ->
            viewModel.viewSelectedDay(day)
        })

        binding.dailyForecast.adapter = adapter

        val hourlyAdapter = HourlyForecastAdapter(context!!, viewModel.isMetric.value ?: true)
        binding.hourlyForecast.adapter = hourlyAdapter
        binding.hourlyForecast.layoutManager = LinearLayoutManager(context!!, LinearLayoutManager.HORIZONTAL, false)


        viewModel.isMetric.observe(this, Observer { isMetric ->
            adapter.mIsMetric = isMetric ?: true
        })

        viewModel.showError.observe(this, Observer { showNetworkError ->
            if (showNetworkError) {
                Toast.makeText(activity, "Network Error", Toast.LENGTH_LONG).show()
                viewModel.onNetworkErrorShown()
            }
        })

        viewModel.viewSelectedDay.observe(this, Observer {
            if (it != null) {
                this.findNavController().navigate(HomeFragmentDirections.actionHomeToDayDetails(it))
                viewModel.viewSelectedDayComplete()
            }
        })

        PreferenceManager.getDefaultSharedPreferences(activity)
            .registerOnSharedPreferenceChangeListener(this)
        setHasOptionsMenu(true)
        return binding.root
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, pref: String) {
        if (pref == resources.getString(R.string.pref_location_key)) {
            viewModel.onLocation(
                sharedPreferences.getString(
                    pref,
                    resources.getString(R.string.pref_location_default)
                )!!
            )
        } else if (pref == resources.getString(R.string.pref_units_key)) {
            val isMetric = sharedPreferences.getString(pref, "") == resources.getString(R.string.pref_units_metric)
            viewModel.onUnitChanged(isMetric)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        PreferenceManager.getDefaultSharedPreferences(activity /* Activity context */)
            .unregisterOnSharedPreferenceChangeListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.refresh) {
            viewModel.refresh()
            return true
        }

        return NavigationUI.onNavDestinationSelected(
            item,
            view!!.findNavController()
        ) || super.onOptionsItemSelected(item)
    }
}
