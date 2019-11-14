package com.example.myweather.repository

interface WorkManagerRepository {

    fun enableHourlySync()
    fun cancelHourlySync()
}