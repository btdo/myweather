package com.example.myweather.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.example.myweather.databinding.FragmentDayDetailsBinding


/**
 *
 */
class DayDetailsFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val application = requireNotNull(activity).application
        val selectedDay = DayDetailsFragmentArgs.fromBundle(arguments!!).forecastItem
        val viewModelFactory = DayDetailsViewModelFactory(application, selectedDay)

        val binding = FragmentDayDetailsBinding.inflate(inflater)
        binding.lifecycleOwner = this
        binding.viewModel = ViewModelProviders.of(this, viewModelFactory).get(DayDetailsViewModel::class.java)

        // Inflate the layout for this fragment
        return binding.root
    }
}
