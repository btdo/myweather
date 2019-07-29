package com.example.myweather.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface ForecastItemDao {
    @Insert
    fun insert(item: ForecastItemEntity)

    @Update
    fun update(item: ForecastItemEntity)

    @Query("SELECT * from forecast_item_table WHERE date = :date and city = :city")
    fun query(date: Long, city: String): ForecastItemEntity

    @Query("Delete from forecast_item_table")
    fun clear()

    @Query("SELECT * from forecast_item_table where date >= :date and city = :city ORDER BY date ASC")
    fun queryFutureWeatherItems(date: Long, city: String): List<ForecastItemEntity>
}
