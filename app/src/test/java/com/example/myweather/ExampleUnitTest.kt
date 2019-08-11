package com.example.myweather

import com.example.myweather.utils.SunshineDateUtils
import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun dateConversion2() {
        val dt = 1565362800L
        val date = dt * 1000
        val dayFormat = SimpleDateFormat("MM.dd HH:mm")
        val localTime = dayFormat.format(date)
        Assert.assertEquals("08.09 11:00", localTime)
    }

    @Test
    fun testGetNextDayMidday() {
        //dateTimeFormat = yyyy-MM-dd HH:mm:ss
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.add(Calendar.DATE, 1) // Add daysCount days to current date
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.HOUR, 0)
        calendar.set(Calendar.AM_PM, Calendar.PM)
        val st = SunshineDateUtils.getNextDaysNoon(1)
        val elapsedHours = elapsedHoursSinceEpoch(st)
        val testHours = elapsedHoursSinceEpoch(calendar.timeInMillis)
        Assert.assertEquals(testHours, elapsedHours)

    }

    @Test
    fun getCurrentHour() {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val currentHour = SunshineDateUtils.getCurrentHour()
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss a")
        val elapsedHours = elapsedHoursSinceEpoch(currentHour)
        val testHours = elapsedHoursSinceEpoch(calendar.timeInMillis)
        Assert.assertEquals(testHours, elapsedHours)
    }

    @Test
    fun getDaysBetween() {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.add(Calendar.DATE, 1) // Add daysCount days to current date
        Assert.assertEquals(1, SunshineDateUtils.getNumDaysBetweenNow(calendar.timeInMillis))
    }

    /**
     * This method returns the number of days since the epoch (January 01, 1970, 12:00 Midnight UTC)
     * in UTC time from the current date.
     *
     * @param utcDate A date in milliseconds in UTC time.
     *
     * @return The number of days from the epoch to the date argument.
     */
    fun elapsedHoursSinceEpoch(utcDate: Long): Long {
        return TimeUnit.MILLISECONDS.toHours(utcDate)
    }


}
