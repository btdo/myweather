package com.example.myweather.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.myweather.R
import com.example.myweather.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {


    private val viewModel: HomeFragmentViewModel by lazy {
        val activity = requireNotNull(this.activity) {
            "You can only access the viewModel after onActivityCreated()"
        }
        ViewModelProviders.of(this, HomeFragmentViewModel.Factory(activity.application)).get(HomeFragmentViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentHomeBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_home,
            container,
            false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.showNetworkError.observe(this, Observer {showNetworkError ->
            if (showNetworkError){
                Toast.makeText(activity, "Network Error", Toast.LENGTH_LONG).show()
                viewModel.onNetworkErrorShown()
            }
        })

        return binding.root
    }

}
