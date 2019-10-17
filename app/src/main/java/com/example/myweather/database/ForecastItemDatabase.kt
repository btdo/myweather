package com.example.myweather.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.myweather.R
import com.example.myweather.utils.Json

@Database(entities = [ForecastItemEntity::class, LocationEntity::class], version = 1, exportSchema = false)
abstract class ForecastItemDatabase : RoomDatabase() {

    abstract val forecastItemDao: ForecastItemDao
    abstract val locationDao: LocationDao

    companion object {

        @Volatile
        private var INSTANCE: ForecastItemDatabase? = null

        fun getInstance(context: Context): ForecastItemDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        ForecastItemDatabase::class.java,
                        "forecast_item_database"
                    ).addCallback(object : RoomDatabase.Callback() {
                    })
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }

        private fun prepopulateDb(context: Context, db: ForecastItemDatabase) {
            val cityListJson = Json.readFromResources(context.applicationContext, R.raw.city_list)
            val cities = mutableListOf<LocationEntity>()
            for (i in 0 until cityListJson.length()) {
                val jsonObj = cityListJson.getJSONObject(i)
                val city = jsonObj.asCityLocationEntity()
                cities.add(city)
            }
            db.locationDao.insertAll(cities)
        }
    }
}