package com.example.attendanceplus.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.attendanceplus.Repository
import com.example.attendanceplus.database.Attendance
import com.example.attendanceplus.database.AttendanceStatus
import com.example.attendanceplus.database.Schedule
import com.example.attendanceplus.database.Subject
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar

@HiltViewModel
class TimetableViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {
    private val _currentWeek = MutableStateFlow(getStartOfWeek(System.currentTimeMillis()))
    val currentWeek: StateFlow<Long> = _currentWeek.asStateFlow()

    private val _subjectMap = MutableStateFlow<Map<Long, Subject>>(emptyMap())
    val subjectMap: StateFlow<Map<Long, Subject>> = _subjectMap.asStateFlow()

    private val _scheduleCache = MutableStateFlow<Map<Int, List<Schedule>>?>(null)

    private val _attendanceCache = mutableMapOf<Long, Map<Long, Attendance>>()

    private val _timetableState = MutableStateFlow<TimetableState>(TimetableState.Loading)
    val timetableState: StateFlow<TimetableState> = _timetableState.asStateFlow()

    init {
        loadSubjectMap()
        loadScheduleCache()
        loadTimetableDataForWeek(_currentWeek.value)
    }

    private fun loadScheduleCache() {
        viewModelScope.launch {
            val schedules = repository.getAllSchedules().first()
            _scheduleCache.value = schedules.groupBy { it.dayOfWeek }
        }
    }

    fun setWeek(weekStart: Long) {
        _currentWeek.value = weekStart

        val schedule = _scheduleCache.value
        val attendance = _attendanceCache[weekStart]

        when {
            schedule != null && attendance != null -> {
                _timetableState.value = TimetableState.Success(
                    scheduleByDay = schedule,
                    attendanceBySchedule = attendance
                )
            }
            schedule != null -> {
                _timetableState.value = TimetableState.Success(
                    scheduleByDay = schedule,
                    attendanceBySchedule = emptyMap()
                )
                loadAttendanceForWeek(weekStart)
            }
            else -> {
                _timetableState.value = TimetableState.Loading
                loadTimetableDataForWeek(weekStart)
            }
        }
    }

    private fun loadTimetableDataForWeek(weekStart: Long) {
        viewModelScope.launch {
            try {
                var schedule = _scheduleCache.value
                if (schedule == null) {
                    val schedules = repository.getAllSchedules().first()
                    schedule = schedules.groupBy { it.dayOfWeek }
                    _scheduleCache.value = schedule
                }

                loadAttendanceForWeek(weekStart)
            } catch (e: Exception) {
                _timetableState.value = TimetableState.Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun loadAttendanceForWeek(weekStart: Long) {
        viewModelScope.launch {
            try {
                val attendanceRecords = repository.getWeeklyAttendance(weekStart).first()
                val attendanceMap = attendanceRecords.associateBy { it.scheduleId }

                _attendanceCache[weekStart] = attendanceMap

                if (_currentWeek.value == weekStart) {
                    _scheduleCache.value?.let { schedule ->
                        _timetableState.value = TimetableState.Success(
                            scheduleByDay = schedule,
                            attendanceBySchedule = attendanceMap
                        )
                    }
                }
            } catch (e: Exception) {
                _timetableState.value = TimetableState.Error(e.message ?: "Failed to load attendance")
            }
        }
    }

    fun updateAttendanceStatus(scheduleId: Long, status: AttendanceStatus) {
        viewModelScope.launch {
            val week = _currentWeek.value

            updateLocalCache(week, scheduleId, status)

            repository.getAttendanceRecord(week, scheduleId)?.let {
                repository.updateAttendanceStatus(week, scheduleId, status)
            } ?: run {
                repository.markAttendance(
                    Attendance(
                        weekStartDate = week,
                        scheduleId = scheduleId,
                        status = status
                    )
                )
            }
        }
    }

    private fun updateLocalCache(week: Long, scheduleId: Long, status: AttendanceStatus) {
        val currentAttendance = _attendanceCache[week] ?: emptyMap()

        val newAttendanceMap = currentAttendance.toMutableMap()
        newAttendanceMap[scheduleId] = newAttendanceMap[scheduleId]?.copy(
            status = status
        ) ?: Attendance(
            weekStartDate = week,
            scheduleId = scheduleId,
            status = status
        )

        _attendanceCache[week] = newAttendanceMap

        if (week == _currentWeek.value) {
            _scheduleCache.value?.let { schedule ->
                _timetableState.value = TimetableState.Success(
                    scheduleByDay = schedule,
                    attendanceBySchedule = newAttendanceMap
                )
            }
        }
    }

    fun loadSubjectMap(){
        viewModelScope.launch {
            repository.getAllSubjects().collect { _subjectMap.value = it.associateBy { subject -> subject.id } }
        }
    }

    fun updateAttendanceCount(subjectId: Long, type: Int) {
        viewModelScope.launch {
            when (type) {
                0 -> repository.incrementPresent(subjectId)
                1 -> repository.incrementAbsent(subjectId)
                2 -> repository.decrementPresent(subjectId)
                3 -> repository.decrementAbsent(subjectId)
                else -> error("Invalid attendance type")
            }
        }
    }

    fun getStartOfWeek(currentTimeMillis: Long): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = currentTimeMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }
        return calendar.timeInMillis
    }
}

sealed class TimetableState {
    object Loading : TimetableState()
    data class Success(
        val scheduleByDay: Map<Int, List<Schedule>>,
        val attendanceBySchedule: Map<Long, Attendance>
    ) : TimetableState()
    data class Error(val message: String) : TimetableState()
}