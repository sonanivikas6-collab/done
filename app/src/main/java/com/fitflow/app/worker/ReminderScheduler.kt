package com.fitflow.app.worker

import android.content.Context
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.Calendar
import java.util.concurrent.TimeUnit

object ReminderScheduler {

    fun scheduleDailyReminder(
        context: Context,
        habitId: Long,
        habitName: String,
        hour: Int,
        minute: Int
    ) {
        val delay = calculateInitialDelay(hour, minute)
        val data = Data.Builder()
            .putString(ReminderWorker.KEY_HABIT_NAME, habitName)
            .build()

        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(data)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            uniqueWorkName(habitId),
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun cancelReminder(context: Context, habitId: Long) {
        WorkManager.getInstance(context).cancelUniqueWork(uniqueWorkName(habitId))
    }

    private fun uniqueWorkName(habitId: Long) = "habit_reminder_$habitId"

    private fun calculateInitialDelay(hour: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        if (target.before(now)) {
            target.add(Calendar.DAY_OF_YEAR, 1)
        }
        return target.timeInMillis - now.timeInMillis
    }
}
