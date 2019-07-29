package com.example.myweather.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ForecastItemEntity::class], version = 1, exportSchema = false)
abstract class ForecastItemDatabase : RoomDatabase() {

    abstract val forecastItemDao: ForecastItemDao

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
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }
    }
}