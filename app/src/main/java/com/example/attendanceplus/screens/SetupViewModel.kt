package com.example.attendanceplus.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.attendanceplus.Repository
import com.example.attendanceplus.database.Schedule
import com.example.attendanceplus.database.Subject
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class SetupViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {
    private val _subjects = MutableStateFlow(emptyList<Subject>())
    val subjects: StateFlow<List<Subject>> = _subjects.asStateFlow()

    private val _schedule = MutableStateFlow(emptyMap<Int, List<Schedule>>())
    val schedule: StateFlow<Map<Int, List<Schedule>>> = _schedule.asStateFlow()

    // Track setup completion state
    private val _isSetupComplete = MutableStateFlow(false)
    val isSetupComplete: StateFlow<Boolean> = _isSetupComplete.asStateFlow()

    init {
        checkSetupState()
        loadSubjects()
        loadSchedule()
    }

    private fun checkSetupState() {
        viewModelScope.launch {
            val hasSubjects = repository.getAllSubjects().first().isNotEmpty()
            val hasSchedule = repository.getAllSchedules().first().isNotEmpty()
            _isSetupComplete.value = hasSubjects && hasSchedule
        }
    }

    private fun loadSubjects() {
        viewModelScope.launch {
            repository.getAllSubjects().collect { _subjects.value = it }
        }
    }

    private fun loadSchedule() {
        viewModelScope.launch {
            repository.getAllSchedules().collect {
                _schedule.value = it.groupBy { schedule -> schedule.dayOfWeek }
            }
        }
    }

    fun addSubject(name: String) {
        viewModelScope.launch {
            repository.addSubject(Subject(name = name))
        }
    }

    fun deleteSubject(id: Long) {
        viewModelScope.launch {
            repository.deleteSubject(id)
        }
    }

    fun addScheduleEntry(day: Int, subjectId: Long, position: Int) {
        viewModelScope.launch {
            repository.addScheduleEntry(
                Schedule(
                    dayOfWeek = day,
                    subjectId = subjectId,
                    timeSlot = position
                )
            )
        }
    }

    fun completeSetup() {
        viewModelScope.launch {
            // Verify we have at least one subject and schedule entry
            val hasSubjects = _subjects.value.isNotEmpty()
            val hasSchedule = _schedule.value.values.any { it.isNotEmpty() }

            if (hasSubjects && hasSchedule) {
                _isSetupComplete.value = true
            }
        }
    }
}