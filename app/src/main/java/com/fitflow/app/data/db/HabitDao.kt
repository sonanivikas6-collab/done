package com.fitflow.app.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HabitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long

    @Delete
    suspend fun deleteHabit(habit: Habit)

    @Query("SELECT * FROM habits ORDER BY id ASC")
    fun observeHabits(): Flow<List<Habit>>

    @Query("SELECT * FROM habits ORDER BY id ASC")
    suspend fun getHabitsOnce(): List<Habit>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun markComplete(completion: HabitCompletion)

    @Query("DELETE FROM habit_completions WHERE habitId = :habitId AND dateKey = :dateKey")
    suspend fun markIncomplete(habitId: Long, dateKey: String)

    @Query("SELECT * FROM habit_completions WHERE dateKey = :dateKey")
    fun observeCompletionsForDate(dateKey: String): Flow<List<HabitCompletion>>

    @Query("SELECT * FROM habit_completions WHERE habitId = :habitId ORDER BY dateKey DESC")
    suspend fun getCompletionsForHabit(habitId: Long): List<HabitCompletion>
}
