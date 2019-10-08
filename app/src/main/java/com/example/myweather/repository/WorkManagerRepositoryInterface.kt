package com.example.myweather.repository

interface WorkManagerRepositoryInterface {

    fun enableHourlySync()
    fun cancelHourlySync()
}