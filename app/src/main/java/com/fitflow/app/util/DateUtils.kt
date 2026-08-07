package com.fitflow.app.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {

    private val keyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val displayFormat = SimpleDateFormat("EEE, MMM d", Locale.US)
    private val dayLabelFormat = SimpleDateFormat("EEE", Locale.US)

    fun todayKey(): String = keyFormat.format(Date())

    fun keyFor(date: Date): String = keyFormat.format(date)

    fun displayToday(): String = displayFormat.format(Date())

    fun lastNDaysKeys(days: Int): List<String> {
        val cal = Calendar.getInstance()
        val result = mutableListOf<String>()
        for (i in (days - 1) downTo 0) {
            val c = cal.clone() as Calendar
            c.add(Calendar.DAY_OF_YEAR, -i)
            result.add(keyFormat.format(c.time))
        }
        return result
    }

    fun dayLabel(dateKey: String): String {
        return try {
            val date = keyFormat.parse(dateKey) ?: return dateKey
            dayLabelFormat.format(date)
        } catch (e: Exception) {
            dateKey
        }
    }

    fun isToday(dateKey: String): Boolean = dateKey == todayKey()
}
