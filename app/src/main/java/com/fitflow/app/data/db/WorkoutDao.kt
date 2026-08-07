package com.fitflow.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {

    @Insert
    suspend fun insert(session: WorkoutSession)

    @Query("SELECT * FROM workout_sessions ORDER BY id DESC LIMIT :limit")
    fun observeRecent(limit: Int = 20): Flow<List<WorkoutSession>>

    @Query("SELECT COUNT(*) FROM workout_sessions WHERE dateKey = :dateKey")
    suspend fun countForDate(dateKey: String): Int

    @Query("SELECT DISTINCT dateKey FROM workout_sessions ORDER BY dateKey DESC")
    suspend fun getWorkoutDates(): List<String>
}
