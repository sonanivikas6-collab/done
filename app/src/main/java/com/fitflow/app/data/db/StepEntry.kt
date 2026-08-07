package com.fitflow.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "step_entries")
data class StepEntry(
    @PrimaryKey
    val dateKey: String,
    val steps: Int,
    val goal: Int = 8000
)
