package com.fitflow.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fitflow.app.data.db.AppDatabase
import com.fitflow.app.data.db.Habit
import com.fitflow.app.data.repository.FitFlowRepository
import com.fitflow.app.util.DateUtils
import com.fitflow.app.util.StepCounterManager
import com.fitflow.app.util.StepMetrics
import com.fitflow.app.util.UserPreferences
import com.fitflow.app.worker.ReminderScheduler
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HabitUiState(
    val habit: Habit,
    val completedToday: Boolean,
    val streak: Int
)

data class TimelineDay(
    val dateKey: String,
    val dayLabel: String,
    val steps: Int,
    val calories: Int,
    val distanceKm: Double
)

data class DashboardUiState(
    val todaySteps: Int = 0,
    val stepGoal: Int = 8000,
    val todayCalories: Int = 0,
    val todayDistanceKm: Double = 0.0,
    val todayMinutes: Int = 0,
    val weekSteps: List<Pair<String, Int>> = emptyList(),
    val timeline: List<TimelineDay> = emptyList(),
    val weekTotalSteps: Int = 0,
    val habits: List<HabitUiState> = emptyList(),
    val workoutDoneToday: Boolean = false,
    val sensorAvailable: Boolean = true,
    val displayName: String = "Guest"
)

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = FitFlowRepository(AppDatabase.getInstance(application))
    private val stepCounterManager = StepCounterManager(application)
    private val userPreferences = UserPreferences(application)

    private val _stepGoal = MutableStateFlow(8000)
    private val _workoutDoneToday = MutableStateFlow(false)
    private val _habitStreaks = MutableStateFlow<Map<Long, Int>>(emptyMap())
    private val _displayName = MutableStateFlow("Guest")

    val sensorAvailable: Boolean get() = stepCounterManager.isSensorAvailable

    init {
        stepCounterManager.start()
        viewModelScope.launch {
            _stepGoal.value = repository.getTodayGoal()
        }
        viewModelScope.launch {
            userPreferences.displayName.collect { _displayName.value = it }
        }
        refreshWorkoutStatus()
        viewModelScope.launch {
            stepCounterManager.todaySteps.collect { steps ->
                repository.saveTodaySteps(steps, _stepGoal.value)
            }
        }
        viewModelScope.launch {
            repository.observeHabits().collect { habits ->
                val streaks = habits.associate { it.id to repository.getStreak(it.id) }
                _habitStreaks.value = streaks
            }
        }
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        stepCounterManager.todaySteps,
        _stepGoal,
        repository.observeRecentSteps(30),
        repository.observeHabits(),
        repository.observeTodayCompletions(),
        _workoutDoneToday,
        _habitStreaks,
        _displayName
    ) { flows ->
        @Suppress("UNCHECKED_CAST")
        val todaySteps = flows[0] as Int
        val goal = flows[1] as Int
        val recent = flows[2] as List<com.fitflow.app.data.db.StepEntry>
        val habits = flows[3] as List<Habit>
        val completions = flows[4] as List<com.fitflow.app.data.db.HabitCompletion>
        val workoutDone = flows[5] as Boolean
        val streaks = flows[6] as Map<Long, Int>
        val name = flows[7] as String

        val completedIds = completions.map { it.habitId }.toSet()
        val recentMap = recent.associateBy { it.dateKey }

        val weekKeys = DateUtils.lastNDaysKeys(7)
        val weekSteps = weekKeys.map { key ->
            val stepsForDay = if (DateUtils.isToday(key)) todaySteps else (recentMap[key]?.steps ?: 0)
            DateUtils.dayLabel(key) to stepsForDay
        }
        val weekTotal = weekSteps.sumOf { it.second }

        val timelineKeys = DateUtils.lastNDaysKeys(30).reversed()
        val timeline = timelineKeys.mapNotNull { key ->
            val stepsForDay = if (DateUtils.isToday(key)) todaySteps else recentMap[key]?.steps
            if (stepsForDay == null) return@mapNotNull null
            TimelineDay(
                dateKey = key,
                dayLabel = if (DateUtils.isToday(key)) "Today" else DateUtils.dayLabel(key),
                steps = stepsForDay,
                calories = StepMetrics.calories(stepsForDay),
                distanceKm = StepMetrics.distanceKm(stepsForDay)
            )
        }

        DashboardUiState(
            todaySteps = todaySteps,
            stepGoal = goal,
            todayCalories = StepMetrics.calories(todaySteps),
            todayDistanceKm = StepMetrics.distanceKm(todaySteps),
            todayMinutes = StepMetrics.estimatedMinutes(todaySteps),
            weekSteps = weekSteps,
            timeline = timeline,
            weekTotalSteps = weekTotal,
            habits = habits.map { h ->
                HabitUiState(
                    habit = h,
                    completedToday = completedIds.contains(h.id),
                    streak = streaks[h.id] ?: 0
                )
            },
            workoutDoneToday = workoutDone,
            sensorAvailable = sensorAvailable,
            displayName = name
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardUiState())

    fun setStepGoal(goal: Int) {
        _stepGoal.value = goal
        viewModelScope.launch {
            repository.saveTodaySteps(stepCounterManager.todaySteps.value, goal)
        }
    }

    fun addHabit(name: String, emoji: String, reminderHour: Int?, reminderMinute: Int?) {
        viewModelScope.launch {
            val id = repository.addHabit(name, emoji)
            if (reminderHour != null && reminderMinute != null) {
                ReminderScheduler.scheduleDailyReminder(
                    getApplication(), id, name, reminderHour, reminderMinute
                )
            }
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            ReminderScheduler.cancelReminder(getApplication(), habit.id)
            repository.deleteHabit(habit)
        }
    }

    fun toggleHabit(habitId: Long, currentlyComplete: Boolean) {
        viewModelScope.launch {
            repository.toggleHabitToday(habitId, currentlyComplete)
        }
    }

    fun logWorkout(name: String, durationSeconds: Int, calories: Int) {
        viewModelScope.launch {
            repository.logWorkout(name, durationSeconds, calories)
            refreshWorkoutStatus()
        }
    }

    private fun refreshWorkoutStatus() {
        viewModelScope.launch {
            _workoutDoneToday.value = repository.didWorkoutToday()
        }
    }

    override fun onCleared() {
        super.onCleared()
        stepCounterManager.stop()
    }
}
