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

    @Query("SELECT * from forecast_item_table WHERE date = :date and location = :location")
    fun query(date: Long, location: String): ForecastItemEntity?

    @Query("Delete from forecast_item_table WHERE date < :date and location = :location")
    fun clearPastItems(location: String, date: Long)

    @Query("Delete from forecast_item_table WHERE date > :date and location = :location")
    fun clearFutureItems(location: String, date: Long)

    @Query("Delete from forecast_item_table WHERE location = :location")
    fun clear(location: String)

    @Query("Delete from forecast_item_table")
    fun clearAll()

    @Query("SELECT * from forecast_item_table where date >= :date and location = :location ORDER BY date ASC")
    fun queryFutureWeatherItems(date: Long, location: String): List<ForecastItemEntity>
}
