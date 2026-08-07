package com.fitflow.app.data.db

import androidx.room.Entity

@Entity(
    tableName = "habit_completions",
    primaryKeys = ["habitId", "dateKey"]
)
data class HabitCompletion(
    val habitId: Long,
    val dateKey: String
)
