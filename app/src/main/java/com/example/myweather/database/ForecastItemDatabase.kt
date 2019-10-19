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
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            Thread(Runnable { prepopulateDb(context, getInstance(context)) }).start()
                        }
                    })
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                }
                return instance
            }
        }

        private fun prepopulateDb(context: Context, db: ForecastItemDatabase) {
            readFromResources(context.applicationContext, R.raw.city_list, db)
        }

        private fun readFromResources(context: Context, resource: Int, db: ForecastItemDatabase) {
            var reader: JsonReader? = null
            try {
                val inputStream: InputStream = context.resources.openRawResource(resource)
                reader = JsonReader(InputStreamReader(inputStream, "UTF-8"))
                reader.beginArray()
                val gson = GsonBuilder().create()
                while (reader.hasNext()) {
                    val cityJson = gson.fromJson(reader, CityJson::class.java) as CityJson
                    val city = cityJson.asCityLocationEntity()
                    db.locationDao.insert(city)
                }
            } catch (e: Throwable) {
                Timber.e(e)
            } finally {
                reader?.let { reader.close() }
            }
        }


    }
}

data class CityJson(val json: JSONObject) {
    var id: Int
    var country: String
    var coord: CoordJson
    var name: String

    init {
        id = json.optInt("id")
        country = json.optString("country")
        coord = CoordJson(json.optJSONObject("coord"))
        name = json.optString("name")
    }
}

fun CityJson.asCityLocationEntity(): LocationEntity {
    return LocationEntity(coord.lat, coord.lon, country, id, name, null)
}

data class CoordJson(val json: JSONObject) : Serializable {
    @SerializedName("lon")
    var lon: Double
    @SerializedName("lat")
    var lat: Double

    init {
        lon = json.optDouble("lon")
        lat = json.optDouble("lat")
    }
}