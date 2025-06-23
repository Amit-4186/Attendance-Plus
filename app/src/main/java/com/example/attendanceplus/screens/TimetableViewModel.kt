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

    // Cache for timetable states by week
    private val _timetableCache = mutableMapOf<Long, TimetableState>()

    // Current timetable state
    private val _timetableState = MutableStateFlow<TimetableState>(TimetableState.Loading)
    val timetableState: StateFlow<TimetableState> = _timetableState.asStateFlow()

    init {
        loadSubjectMap()
        loadTimetableDataForWeek(_currentWeek.value)
    }

    fun setWeek(weekStart: Long) {
        _currentWeek.value = weekStart

        // Check cache first
        _timetableCache[weekStart]?.let { cachedState ->
            _timetableState.value = cachedState
        } ?: run {
            // Not in cache, load from database
            _timetableState.value = TimetableState.Loading
            loadTimetableDataForWeek(weekStart)
        }
    }

    private fun loadTimetableDataForWeek(weekStart: Long) {
        viewModelScope.launch {
            try {
                // Load schedules once (they don't change per week)
                val schedules = repository.getAllSchedules().first()
                val scheduleMap = schedules.groupBy { it.dayOfWeek }

                // Load attendance for specific week
                val attendanceRecords = repository.getWeeklyAttendance(weekStart).first()
                val attendanceMap = attendanceRecords.associateBy { it.scheduleId }

                val state = TimetableState.Success(
                    scheduleByDay = scheduleMap,
                    attendanceBySchedule = attendanceMap
                )

                // Update cache and state
                _timetableCache[weekStart] = state
                if (_currentWeek.value == weekStart) {
                    _timetableState.value = state
                }
            } catch (e: Exception) {
                // Handle error
                _timetableState.value = TimetableState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun updateAttendanceStatus(scheduleId: Long, status: AttendanceStatus) {
        viewModelScope.launch {
            val week = _currentWeek.value

            // Optimistic UI update
            updateLocalCache(week, scheduleId, status)

            // Persist to database
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
        val currentState = _timetableCache[week]
        if (currentState is TimetableState.Success) {
            val newAttendanceMap = currentState.attendanceBySchedule.toMutableMap()
            newAttendanceMap[scheduleId] = newAttendanceMap[scheduleId]?.copy(
                status = status
            ) ?: Attendance(
                weekStartDate = week,
                scheduleId = scheduleId,
                status = status
            )

            val newState = currentState.copy(
                attendanceBySchedule = newAttendanceMap
            )

            // Update cache
            _timetableCache[week] = newState

            // Update current state if we're viewing this week
            if (week == _currentWeek.value) {
                _timetableState.value = newState
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