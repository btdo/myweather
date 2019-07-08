package com.example.myweather.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.example.myweather.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private val viewModel: HomeFragmentViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProviders.of(this, HomeFragmentViewModel.Factory(activity.application))
            .get(HomeFragmentViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = FragmentHomeBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        val adapter = ForecastAdapter(context!!, viewModel.isMetric.value ?: true, ForecastClickListener { day ->
            viewModel.viewSelectedDay(day)
        })

        binding.forecast.adapter = adapter

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

        return binding.root
    }
}
