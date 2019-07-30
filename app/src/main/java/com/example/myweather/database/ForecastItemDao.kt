package com.example.myweather.database

import androidx.room.*

@Dao
interface ForecastItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: ForecastItemEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(videos: List<ForecastItemEntity>)

    @Update
    fun update(item: ForecastItemEntity)

    @Query("SELECT * from forecast_item_table WHERE date = :date and city = :city")
    fun query(date: Long, city: String): ForecastItemEntity?

    @Query("Delete from forecast_item_table")
    fun clear()

    @Query("Delete from forecast_item_table WHERE city = :city")
    fun clear(city: String)

    @Query("SELECT * from forecast_item_table where date >= :date and city = :city ORDER BY date ASC")
    fun queryFutureWeatherItems(date: Long, city: String): List<ForecastItemEntity>
}
