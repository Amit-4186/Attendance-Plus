package com.example.attendanceplus

import com.example.attendanceplus.database.Attendance
import com.example.attendanceplus.database.AttendanceDao
import com.example.attendanceplus.database.AttendanceStatus
import com.example.attendanceplus.database.Schedule
import com.example.attendanceplus.database.ScheduleDao
import com.example.attendanceplus.database.Subject
import com.example.attendanceplus.database.SubjectDao
import jakarta.inject.Inject

class Repository @Inject constructor(
    private val subjectDao: SubjectDao,
    private val scheduleDao: ScheduleDao,
    private val attendanceDao: AttendanceDao
) {
    // Subjects
    suspend fun addSubject(subject: Subject) = subjectDao.insert(subject)
    fun getAllSubjects() = subjectDao.getAllSubjects()
    suspend fun deleteSubject(id: Long) = subjectDao.delete(id)

    // Schedule
    suspend fun addScheduleEntry(schedule: Schedule) = scheduleDao.insert(schedule)
    fun getDailySchedule(day: Int) = scheduleDao.getScheduleForDay(day)
    fun getAllSchedules() = scheduleDao.getAllSchedules()
    suspend fun clearSchedule() = scheduleDao.clearAll()

    // Attendance
    suspend fun markAttendance(attendance: Attendance) = attendanceDao.insert(attendance)
    suspend fun updateAttendanceStatus(weekStart: Long, scheduleId: Long, status: AttendanceStatus) {
        attendanceDao.updateStatus(weekStart, scheduleId, status)
    }
    fun getWeeklyAttendance(weekStart: Long) = attendanceDao.getAttendanceForWeek(weekStart)
    suspend fun getAttendanceRecord(weekStart: Long, scheduleId: Long) =
        attendanceDao.getAttendanceRecord(weekStart, scheduleId)
}