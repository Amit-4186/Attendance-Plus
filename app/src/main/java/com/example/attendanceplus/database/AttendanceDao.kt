package com.example.attendanceplus.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AttendanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(attendance: Attendance)

    @Query("SELECT * FROM attendance WHERE weekStartDate = :weekStart")
    fun getAttendanceForWeek(weekStart: Long): Flow<List<Attendance>>

    @Query("""
        SELECT * FROM attendance 
        WHERE weekStartDate = :weekStart AND scheduleId = :scheduleId
    """)
    suspend fun getAttendanceRecord(weekStart: Long, scheduleId: Long): Attendance?

    @Query("""
        UPDATE attendance SET status = :status 
        WHERE weekStartDate = :weekStart AND scheduleId = :scheduleId
    """)
    suspend fun updateStatus(weekStart: Long, scheduleId: Long, status: AttendanceStatus)
}