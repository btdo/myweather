package com.example.myweather.di

import androidx.lifecycle.ViewModel
import com.example.myweather.ui.HomeFragmentViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(HomeFragmentViewModel::class)
    abstract fun bindMyViewModel(homeFragmentModel: HomeFragmentViewModel): ViewModel
}