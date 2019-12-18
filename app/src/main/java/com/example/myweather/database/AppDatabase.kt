package com.example.myweather.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myweather.R
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.google.gson.stream.JsonReader
import org.json.JSONObject
import timber.log.Timber
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Serializable

@Database(entities = [ForecastItemEntity::class, LocationEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract val forecastItemDao: ForecastItemDao
    abstract val locationDao: LocationDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            synchronized(this) {
                var instance = INSTANCE

                if (instance == null) {
                    instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "forecast_item_database"
                    ).addCallback(object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Thread(Runnable { prepopulateDb(context, getInstance(context)) }).start()
                        }
                    }).fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }

        private fun prepopulateDb(context: Context, db: AppDatabase) {
            readFromResources(context.applicationContext, R.raw.city_list, db)
        }

        private fun readFromResources(context: Context, resource: Int, db: AppDatabase) {
            var reader: JsonReader? = null
            try {
                val inputStream: InputStream = context.resources.openRawResource(resource)
                reader = JsonReader(InputStreamReader(inputStream, "UTF-8"))
                reader.beginArray()
                val gson = GsonBuilder().create()
                while (reader.hasNext()) {
                    Timber.i("Inserting database")
                    val cityJson = gson.fromJson(reader, CityJson::class.java) as CityJson
                    val city = cityJson.asCityLocationEntity()
                    db.locationDao.insert(city)
                }
            } catch (e: Throwable) {
                Timber.e(e)
            } finally {
                reader?.let { reader.close() }
                Timber.i("Done inserting database")
            }
        }
    }
}

data class CityJson(val json: JSONObject) {
    var id: Int = json.optInt("id")
    var country: String = json.optString("country")
    var coord: CoordJson = CoordJson(json.getJSONObject("coord"))
    var name: String = json.optString("name")

}

fun CityJson.asCityLocationEntity(): LocationEntity {
    return LocationEntity(coord.lat, coord.lon, country, id, name, null)
}

data class CoordJson(val json: JSONObject) : Serializable {
    @SerializedName("lon")
    var lon: Double = json.optDouble("lon")
    @SerializedName("lat")
    var lat: Double = json.optDouble("lat")

}