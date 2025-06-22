package com.example.attendanceplus.database

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "attendance",
    primaryKeys = ["weekStartDate", "scheduleId"],
    foreignKeys = [
        ForeignKey(
            entity = Schedule::class,
            parentColumns = ["id"],
            childColumns = ["scheduleId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Attendance(
    val weekStartDate: Long, // Monday of the week in millis
    val scheduleId: Long,   // Reference to specific schedule occurrence
    val status: AttendanceStatus
)