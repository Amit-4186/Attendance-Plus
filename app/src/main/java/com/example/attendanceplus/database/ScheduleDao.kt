package com.example.attendanceplus.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: Schedule)

    @Query("SELECT * FROM schedules WHERE dayOfWeek = :day ORDER BY timeSlot ASC")
    fun getScheduleForDay(day: Int): Flow<List<Schedule>>

    @Query("SELECT * FROM schedules ORDER BY dayOfWeek, timeSlot")
    fun getAllSchedules(): Flow<List<Schedule>>

    @Query("DELETE FROM schedules")
    suspend fun clearAll()
}