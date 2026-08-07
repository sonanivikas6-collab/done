package com.fitflow.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workout_sessions")
data class WorkoutSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateKey: String,
    val workoutName: String,
    val durationSeconds: Int,
    val caloriesEstimate: Int
)
