package com.example.attendanceplus.database

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "schedules",
    indices = [Index("dayOfWeek", "timeSlot", unique = true)],
    foreignKeys = [
        ForeignKey(
            entity = Subject::class,
            parentColumns = ["id"],
            childColumns = ["subjectId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Schedule(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dayOfWeek: Int, // Calendar.DAY_OF_WEEK constants
    val subjectId: Long,
    val timeSlot: Int // Position in daily schedule (0,1,2,...)
)