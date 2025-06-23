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
import kotlinx.coroutines.flow.combine
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

    // Combined state of schedule + attendance for current week
    private val _timetableState = MutableStateFlow<TimetableState>(TimetableState.Loading)
    val timetableState: StateFlow<TimetableState> = _timetableState.asStateFlow()

    // Attendance summary
//    private val _attendanceSummary = MutableStateFlow(emptyMap<Long, Pair<Int, Int>>())
//    val attendanceSummary: StateFlow<Map<Long, Pair<Int, Int>>> = _attendanceSummary.asStateFlow()

    init {
        loadTimetableData()
        loadSubjectMap()
    }

    fun setWeek(weekStart: Long) {
        _currentWeek.value = weekStart
        loadTimetableData()
    }

    private fun loadTimetableData() {
        viewModelScope.launch {
            _timetableState.value = TimetableState.Loading

            // Combine both flows
            combine(
                repository.getAllSchedules(),
                repository.getWeeklyAttendance(_currentWeek.value)
            ) { schedules, attendanceRecords ->
                val scheduleMap = schedules.groupBy { it.dayOfWeek }
                val attendanceMap = attendanceRecords.associateBy { it.scheduleId }

                TimetableState.Success(
                    scheduleByDay = scheduleMap,
                    attendanceBySchedule = attendanceMap
                )
            }.collect { state ->
                _timetableState.value = state
//                if (state is TimetableState.Success) {
//                    _attendanceSummary.value = calculateAttendance(
//                        state.scheduleByDay.values.flatten(),
//                        state.attendanceBySchedule
//                    )
//                }
            }
        }
    }

    fun loadSubjectMap(){
        viewModelScope.launch {
            repository.getAllSubjects().collect { _subjectMap.value = it.associateBy { subject -> subject.id } }
        }
    }

    fun updateAttendanceStatus(scheduleId: Long, status: AttendanceStatus) {
        viewModelScope.launch {

//            updateLocalState(scheduleId, status)
//            val week = _currentWeek.value
            repository.getAttendanceRecord(_currentWeek.value, scheduleId)?.let {
                repository.updateAttendanceStatus(_currentWeek.value, scheduleId, status)
            } ?: run {
                repository.markAttendance(
                    Attendance(
                        weekStartDate = _currentWeek.value,
                        scheduleId = scheduleId,
                        status = status
                    )
                )
            }
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


//    private fun updateLocalState(scheduleId: Long, status: AttendanceStatus) {
//        val currentState = _timetableState.value
//        if (currentState is TimetableState.Success) {
//            val newAttendanceMap = currentState.attendanceBySchedule.toMutableMap()
//
//            newAttendanceMap[scheduleId] = newAttendanceMap[scheduleId]?.copy(
//                status = status
//            ) ?: Attendance(
//                weekStartDate = _currentWeek.value,
//                scheduleId = scheduleId,
//                status = status
//            )
//
//            _timetableState.value = currentState.copy(
//                attendanceBySchedule = newAttendanceMap
//            )
//
//            // Update summary immediately
//            _attendanceSummary.value = calculateAttendance(
//                currentState.scheduleByDay.values.flatten(),
//                newAttendanceMap
//            )
//        }
//    }

//    private fun calculateAttendance(
//        schedules: List<Schedule>,
//        attendance: Map<Long, Attendance>
//    ): Map<Long, Pair<Int, Int>> {
//        return schedule';s.groupBy { it.subjectId }.mapValues { (_, subjectSchedules) ->
//            val total = subjectSchedules.size
//            if (total == 0) Pair(0,0)
//            else {
//                val presentCount = subjectSchedules.count {
//                    attendance[it.id]?.status == AttendanceStatus.PRESENT
//                }
//                val absentCount = subjectSchedules.count {
//                    attendance[it.id]?.status == AttendanceStatus.ABSENT
//                }
//                Pair(presentCount, absentCount)
//            }
//        }
//    }

//    private fun getStartOfWeek(timeMillis: Long): Long {
//        val calendar = Calendar.getInstance().apply {
//            timeInMillis = timeMillis
//            set(Calendar.HOUR_OF_DAY, 0)
//            clear(Calendar.MINUTE)
//            clear(Calendar.SECOND)
//            clear(Calendar.MILLISECOND)
//            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
//        }
//        return calendar.timeInMillis
//    }

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
}