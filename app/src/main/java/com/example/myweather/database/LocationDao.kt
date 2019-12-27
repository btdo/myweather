package com.example.myweather.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(item: LocationEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(cities: List<LocationEntity>)

    @Query("SELECT * from location_table WHERE country = :country and name = :name")
    fun queryLocation(name: String, country: String): LocationEntity?

    @Query("SELECT * from location_table WHERE name = :name")
    fun queryLocation(name: String): List<LocationEntity>?

}