package com.example.myweather.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.preference.PreferenceManager
import com.example.myweather.R
import com.example.myweather.databinding.FragmentDayDetailsBinding


/**
 * Fragment to show the details of individual days
 */
class DayDetailsFragment : Fragment() {

    private lateinit var binding: FragmentDayDetailsBinding
    private lateinit var viewModel: DayDetailsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity /* Activity context */)
        val isMetric = sharedPreferences.getString(
            resources.getString(R.string.pref_units_key),
            ""
        ) == resources.getString(R.string.pref_units_metric)
        val application = requireActivity().application
        val selectedDay = DayDetailsFragmentArgs.fromBundle(arguments!!).forecastItem
        val viewModelFactory = DayDetailsViewModelFactory(application, selectedDay, isMetric)

        binding = FragmentDayDetailsBinding.inflate(inflater)
        binding.lifecycleOwner = this
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(DayDetailsViewModel::class.java)
        binding.viewModel = viewModel


        val hourlyAdapter = DayDetailsHourlyAdapter(isMetric)
        binding.content.hourlyForecast.adapter = hourlyAdapter

        // Inflate the layout for this fragment
        return binding.root
    }
}
