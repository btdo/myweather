package com.example.myweather.untils

import com.example.myweather.utils.DateUtils
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
class DateUtilsTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    /**
     * This test is to prove that SimpleDateFormat will convert the unix time to local time without any further manipulation
     */
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
        val st = DateUtils.getNextDaysNoon(1)
        val elapsedHours = elapsedHoursSinceEpoch(st)
        val testHours = elapsedHoursSinceEpoch(calendar.timeInMillis)
        Assert.assertEquals(testHours, elapsedHours)
    }

    @Test
    fun testGetNextDay() {
        val dayFormat = SimpleDateFormat("yyyy.MM.dd HH:mm:ss a")
        val localTime1 = dayFormat.format(DateUtils.getNextDay(1))
        val localTime2 = dayFormat.format(DateUtils.getNextDay(2))
        val localTime3 = dayFormat.format(DateUtils.getNextDay(3))
        val localTime4 = dayFormat.format(DateUtils.getNextDay(4))
    }


    @Test
    fun getCurrentHour() {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        val currentHour = DateUtils.getCurrentHour()
        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss a")
        val elapsedHours = elapsedHoursSinceEpoch(currentHour)
        val testHours = elapsedHoursSinceEpoch(calendar.timeInMillis)
        Assert.assertEquals(testHours, elapsedHours)
    }

    @Test
    fun getDaysBetween() {
        val calendar = Calendar.getInstance(TimeZone.getDefault())
        calendar.add(Calendar.DATE, 1) // Add daysCount days to current date
        Assert.assertEquals(1, DateUtils.getNumDaysBetweenNow(calendar.timeInMillis))
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
