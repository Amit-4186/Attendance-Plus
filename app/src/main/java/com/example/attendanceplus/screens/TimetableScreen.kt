package com.example.attendanceplus.screens

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.attendanceplus.database.AttendanceStatus
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.collections.getValue

@Composable
fun TimetableScreen() {
    val viewModel: TimetableViewModel = hiltViewModel()
    val timetableState by viewModel.timetableState.collectAsState()
    val attendanceSummary by viewModel.attendanceSummary.collectAsState()
    val attendanceSummaryLocal = remember { attendanceSummary }
    val subjectMap by viewModel.subjectMap.collectAsState()
    val currentWeek by viewModel.currentWeek.collectAsState()
    val weekInMillis = 7 * 24 * 60 * 60 * 1000L

    val statusMap = mapOf<AttendanceStatus, AttendanceStatus>(
        AttendanceStatus.UNMARKED to AttendanceStatus.PRESENT,
        AttendanceStatus.PRESENT to AttendanceStatus.ABSENT,
        AttendanceStatus.ABSENT to AttendanceStatus.UNMARKED
    )

    val colorMap = mapOf<AttendanceStatus, Color>(
        AttendanceStatus.UNMARKED to Color.Gray,
        AttendanceStatus.PRESENT to Color.Green,
        AttendanceStatus.ABSENT to Color.Red
    )

    val weekMap = mapOf<Int, String>(
        2 to "Mon",
        3 to "Tue",
        4 to "Wed",
        5 to "Thu",
        6 to "Fri",
        7 to "Sat"
    )

    Column {
        WeekSelector(
            currentWeek,
            { viewModel.setWeek(currentWeek - weekInMillis) },
            { viewModel.setWeek(currentWeek + weekInMillis) },
            { calender -> viewModel.setWeek(getStartOfWeek(calender)) })

        when (val state = timetableState) {
            is TimetableState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is TimetableState.Success -> {
                Row(modifier = Modifier.wrapContentHeight()) {
                    Column {
                        for (day in 2..7) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .padding(4.dp)
                                    .height(40.dp)
                                    .width(30.dp)
                            ) {
                                Text(
                                    text = weekMap[day]!!,
                                    modifier = Modifier
                                        .rotate(-90f)
                                        .fillMaxSize()
                                        .wrapContentSize(Alignment.Center),
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                )
                            }
                        }
                    }
                    Column(modifier = Modifier.fillMaxWidth()) {
                        for (day in 2..7) {
                            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                                state.scheduleByDay[day]!!.forEach { schedule ->
                                    Card(modifier = Modifier.height(48.dp).width(60.dp).padding(vertical = 4.dp, horizontal = 1.dp).align(Alignment.CenterVertically),
                                        colors = CardDefaults.cardColors(
                                            containerColor = colorMap[state.attendanceBySchedule[schedule.id]?.status
                                                ?: AttendanceStatus.UNMARKED] ?: Color.Blue
                                        ),
                                        onClick = {
                                            viewModel.updateAttendanceStatus(
                                                schedule.id,
                                                statusMap[state.attendanceBySchedule[schedule.id]?.status]
                                                    ?: AttendanceStatus.PRESENT
                                            )
                                        }
                                    ) {
                                        Text(
                                            subjectMap[schedule.subjectId]!!.name,
                                            modifier = Modifier.fillMaxSize().wrapContentSize(Alignment.Center)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        attendanceSummary.forEach { (subjectId, percentage) ->
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Text(subjectMap[subjectId]!!.name)
                Text("$percentage %")
            }
        }
    }
}

@Composable
fun WeekSelector(
    currentWeek: Long,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    onDateChanged: (Calendar) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    if (showDatePicker) {
        DatePicker(
            context = context,
            currentDate = Calendar.getInstance().apply { timeInMillis = currentWeek },
            onDateSelected = { calendar ->
                onDateChanged(calendar)
                showDatePicker = false
            },
            onDismiss = { showDatePicker = false }
        )
    }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPreviousClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Previous Week")
        }

        Text(
            text = getWeekRange(currentWeek),
            fontSize = 12.sp,
            textAlign = TextAlign.Center
        )

        IconButton(onClick = { showDatePicker = true }) {
            Icon(Icons.Filled.DateRange, "Select Date")
        }
        IconButton(onClick = onNextClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, "Next Week")
        }
    }
}

fun getWeekRange(currentWeek: Long): String {
    val calendar = Calendar.getInstance().apply {
        timeInMillis = currentWeek
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    // Ensure calendar is at Monday
    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    val monday = calendar.time

    // Get Saturday (Monday + 5 days)
    calendar.add(Calendar.DAY_OF_MONTH, 5)
    val saturday = calendar.time

    val formatter = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    return "${formatter.format(monday)} : ${formatter.format(saturday)}"
}


@Composable
private fun DatePicker(
    context: Context,
    currentDate: Calendar,
    onDateSelected: (Calendar) -> Unit,
    onDismiss: () -> Unit
) {
    val initialYear = currentDate.get(Calendar.YEAR)
    val initialMonth = currentDate.get(Calendar.MONTH)
    val initialDay = currentDate.get(Calendar.DAY_OF_MONTH)

    LaunchedEffect(Unit) {
        DatePickerDialog(
            context,
            { _, year, month, day ->
                Calendar.getInstance().apply {
                    set(year, month, day)
                    if (get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                        add(Calendar.DAY_OF_MONTH, +1)
                    }
                    onDateSelected(this)
                }
            },
            initialYear,
            initialMonth,
            initialDay
        ).apply {
            setOnCancelListener { onDismiss() }
            show()
        }
    }
}

fun getStartOfWeek(calendar: Calendar): Long {
    // Clone to avoid mutating original calendar
    val weekStart = calendar.clone() as Calendar

    // Set to Monday
    weekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

    // Reset time fields
    weekStart.set(Calendar.HOUR_OF_DAY, 0)
    weekStart.set(Calendar.MINUTE, 0)
    weekStart.set(Calendar.SECOND, 0)
    weekStart.set(Calendar.MILLISECOND, 0)

    return weekStart.timeInMillis
}
