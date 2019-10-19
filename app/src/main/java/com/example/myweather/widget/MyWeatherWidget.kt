package com.example.myweather.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.preference.PreferenceManager
import com.example.myweather.R
import com.example.myweather.database.AppDatabase
import com.example.myweather.repository.WeatherRepository
import com.example.myweather.utils.WeatherUtils
import kotlinx.coroutines.runBlocking

/**
 * Implementation of App Widget functionality.
 */
class MyWeatherWidget : AppWidgetProvider() {

    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        internal fun updateAppWidget(
            context: Context, appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            runBlocking {
                // Construct the RemoteViews object
                val views = RemoteViews(context.packageName, R.layout.my_weather_widget)
                showInfo(context, views)
                setupButtonUpdate(context, appWidgetId, views)
                // Instruct the widget manager to update the widget
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }

        private suspend fun showInfo(context: Context, views: RemoteViews) {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)
            val isMetric = sharedPreferences.getString(
                context.resources.getString(R.string.pref_units_key),
                ""
            ) == context.resources.getString(R.string.pref_units_metric)
            val location = sharedPreferences.getString(
                context.resources.getString(R.string.pref_location_key),
                context.resources.getString(R.string.pref_location_default)
            ) ?: "Toronto"

            val database = AppDatabase.getInstance(context)
            val repository = WeatherRepository(database)
            val currentWeather = repository.getCurrentForecast(location, false)
            views.setTextViewText(R.id.appwidget_city, location)
            val weatherImageId: Int = WeatherUtils
                .getSmallArtResourceIdForWeatherCondition(currentWeather.weatherId)
            views.setImageViewResource(R.id.appwidget_icon, weatherImageId)
            val temp = WeatherUtils.formatTemperature(context, currentWeather.temp, isMetric)
            views.setTextViewText(R.id.appwidget_temperature, temp)

        }

        private fun setupButtonUpdate(context: Context, appWidgetId: Int, views: RemoteViews) {
            val intentUpdate = Intent(context, MyWeatherWidget::class.java)
            intentUpdate.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val idArray = intArrayOf(appWidgetId)
            intentUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idArray)
            val pendingUpdate =
                PendingIntent.getBroadcast(context, appWidgetId, intentUpdate, PendingIntent.FLAG_UPDATE_CURRENT)
            views.setOnClickPendingIntent(R.id.button_update, pendingUpdate)
        }
    }
}

