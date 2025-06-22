package com.example.attendanceplus.database

import androidx.room.TypeConverter
import java.util.Date

class Converters {
    @TypeConverter
    fun toDate(value: Long?) = value?.let { Date(it) }

    @TypeConverter
    fun fromDate(date: Date?) = date?.time

    @TypeConverter
    fun toStatus(value: Int) = enumValues<AttendanceStatus>()[value]

    @TypeConverter
    fun fromStatus(status: AttendanceStatus) = status.ordinal
}