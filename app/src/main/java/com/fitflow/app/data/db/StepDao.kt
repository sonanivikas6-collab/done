package com.fitflow.app.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface StepDao {

    @Upsert
    suspend fun upsert(entry: StepEntry)

    @Query("SELECT * FROM step_entries WHERE dateKey = :dateKey LIMIT 1")
    suspend fun getForDate(dateKey: String): StepEntry?

    @Query("SELECT * FROM step_entries WHERE dateKey = :dateKey LIMIT 1")
    fun observeForDate(dateKey: String): Flow<StepEntry?>

    @Query("SELECT * FROM step_entries ORDER BY dateKey DESC LIMIT :limit")
    fun observeRecent(limit: Int = 7): Flow<List<StepEntry>>

    @Query("SELECT * FROM step_entries ORDER BY dateKey DESC")
    suspend fun getAll(): List<StepEntry>
}
