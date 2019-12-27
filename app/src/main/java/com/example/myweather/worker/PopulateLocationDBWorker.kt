package com.example.myweather.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myweather.R
import com.example.myweather.database.AppDatabase
import com.example.myweather.database.LocationEntity
import com.example.myweather.repository.SharedPreferencesRepository
import com.example.myweather.repository.SharedPreferencesRepositoryImpl
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import com.google.gson.stream.JsonReader
import org.json.JSONObject
import timber.log.Timber
import java.io.InputStream
import java.io.InputStreamReader
import java.io.Serializable

class PopulateLocationDBWorker(ctx: Context, params: WorkerParameters) :
    CoroutineWorker(ctx, params) {

    companion object {
        const val MY_WEATHER_POPULATE_CITY_NAME = "MY_WEATHER_POPULATE_CITY_NAME"
    }

    val database = AppDatabase.getInstance(applicationContext)

    private val preferencesRepository: SharedPreferencesRepository by lazy {
        SharedPreferencesRepositoryImpl(applicationContext)
    }

    override suspend fun doWork(): Result {
        populateDb(applicationContext, database)
        return Result.success()
    }

    private fun populateDb(context: Context, db: AppDatabase) {
        readFromResources(context.applicationContext, R.raw.city_list, db)
    }

    private fun readFromResources(context: Context, resource: Int, db: AppDatabase) {
        var reader: JsonReader? = null
        Timber.i("Start inserting into MyWeather's location table")
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
            Timber.i("Done inserting into MyWeather's location table")
            preferencesRepository.markLocationDBPopulated()
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