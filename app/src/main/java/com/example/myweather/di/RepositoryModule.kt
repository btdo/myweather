package com.example.myweather.di

import android.app.Application
import com.example.myweather.database.AppDatabase
import com.example.myweather.repository.*
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class RepositoryModule {

    @Provides
    @Singleton
    fun provideWeatherRepository(application: Application): WeatherRepository {
        return WeatherRepositoryImpl(AppDatabase.getInstance(application))
    }

    @Provides
    @Singleton
    fun provideGeoLocationRepository(application: Application): GeoLocationRepository {
        return GeoLocationRepositoryImpl(application)
    }


    @Provides
    @Singleton
    fun provideWorkManagerRepository(application: Application): WorkManagerRepository {
        return WorkManagerRepositoryImpl(application)
    }

    @Provides
    @Singleton
    fun providesSharedPreferencesRepository(application: Application): SharedPreferencesRepository {
        return SharedPreferencesRepositoryImpl(application)
    }


}