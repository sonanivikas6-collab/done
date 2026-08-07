package com.fitflow.app.data.repository

import com.fitflow.app.data.db.AppDatabase
import com.fitflow.app.data.db.Habit
import com.fitflow.app.data.db.HabitCompletion
import com.fitflow.app.data.db.StepEntry
import com.fitflow.app.data.db.WorkoutSession
import com.fitflow.app.util.DateUtils
import kotlinx.coroutines.flow.Flow

class FitFlowRepository(private val db: AppDatabase) {

    fun observeRecentSteps(days: Int = 7) = db.stepDao().observeRecent(days)

    fun observeTodaySteps(): Flow<StepEntry?> = db.stepDao().observeForDate(DateUtils.todayKey())

    suspend fun saveTodaySteps(steps: Int, goal: Int = 8000) {
        db.stepDao().upsert(StepEntry(dateKey = DateUtils.todayKey(), steps = steps, goal = goal))
    }

    suspend fun getTodayGoal(): Int {
        return db.stepDao().getForDate(DateUtils.todayKey())?.goal ?: 8000
    }

    fun observeHabits(): Flow<List<Habit>> = db.habitDao().observeHabits()

    fun observeTodayCompletions(): Flow<List<HabitCompletion>> =
        db.habitDao().observeCompletionsForDate(DateUtils.todayKey())

    suspend fun addHabit(name: String, emoji: String): Long {
        return db.habitDao().insertHabit(Habit(name = name, emoji = emoji))
    }

    suspend fun deleteHabit(habit: Habit) = db.habitDao().deleteHabit(habit)

    suspend fun toggleHabitToday(habitId: Long, currentlyComplete: Boolean) {
        val today = DateUtils.todayKey()
        if (currentlyComplete) {
            db.habitDao().markIncomplete(habitId, today)
        } else {
            db.habitDao().markComplete(HabitCompletion(habitId, today))
        }
    }

    suspend fun getStreak(habitId: Long): Int {
        val completions = db.habitDao().getCompletionsForHabit(habitId).map { it.dateKey }.toSet()
        val today = DateUtils.todayKey()
        val startFromToday = completions.contains(today)

        var streak = 0
        var cursorOffset = if (startFromToday) 0 else 1
        while (true) {
            val key = DateUtils.lastNDaysKeys(cursorOffset + 1)[0]
            if (completions.contains(key)) {
                streak++
                cursorOffset++
            } else {
                break
            }
        }
        return streak
    }

    fun observeRecentWorkouts(limit: Int = 20): Flow<List<WorkoutSession>> =
        db.workoutDao().observeRecent(limit)

    suspend fun logWorkout(name: String, durationSeconds: Int, calories: Int) {
        db.workoutDao().insert(
            WorkoutSession(
                dateKey = DateUtils.todayKey(),
                workoutName = name,
                durationSeconds = durationSeconds,
                caloriesEstimate = calories
            )
        )
    }

    suspend fun didWorkoutToday(): Boolean {
        return db.workoutDao().countForDate(DateUtils.todayKey()) > 0
    }
}
