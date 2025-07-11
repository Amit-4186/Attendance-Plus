package com.example.attendanceplus.screens

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.attendanceplus.database.AttendanceStatus
import com.example.attendanceplus.database.Subject
import com.example.attendanceplus.ui.theme.Absent
import com.example.attendanceplus.ui.theme.Middle
import com.example.attendanceplus.ui.theme.Present
import com.example.attendanceplus.ui.theme.Unmarked
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun TimetableScreen() {
    val viewModel: TimetableViewModel = hiltViewModel()
    val timetableState by viewModel.timetableState.collectAsState()
    val subjectMap by viewModel.subjectMap.collectAsState()
    var subjectMapLocal by remember { mutableStateOf<Map<Long, Subject>>(emptyMap()) }
    val currentWeek by viewModel.currentWeek.collectAsState()

    LaunchedEffect(subjectMap.isEmpty()) {
        subjectMapLocal = subjectMap.toMutableMap()
    }

    val statusMap = mapOf(
        AttendanceStatus.UNMARKED to AttendanceStatus.PRESENT,
        AttendanceStatus.PRESENT to AttendanceStatus.ABSENT,
        AttendanceStatus.ABSENT to AttendanceStatus.UNMARKED
    )

    val colorMap = mapOf(
        AttendanceStatus.UNMARKED to Unmarked,
        AttendanceStatus.PRESENT to Present,
        AttendanceStatus.ABSENT to Absent
    )

    val weekMap = mapOf(
        2 to "Mon", 3 to "Tue", 4 to "Wed",
        5 to "Thu", 6 to "Fri", 7 to "Sat"
    )

    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        WeekSelector(
            currentWeek,
            { viewModel.setWeek(currentWeek - 7 * 24 * 60 * 60 * 1000L) },
            { viewModel.setWeek(currentWeek + 7 * 24 * 60 * 60 * 1000L) },
            { calender -> viewModel.setWeek(getStartOfWeek(calender)) })

        when (val state = timetableState) {
            is TimetableState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            is TimetableState.Error -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${state.message}")
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
                                    .padding(2.dp)
                                    .height(40.dp)
                                    .width(30.dp)
                            ) {
                                Text(
                                    text = weekMap[day] ?: "?",
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

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        for (day in 2..7) {
                            Row {
                                state.scheduleByDay[day]?.forEach { schedule ->
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .height(44.dp)
                                            .width(80.dp)
                                            .padding(2.dp)
                                            .clickable {
                                                viewModel.updateAttendanceStatus(
                                                    schedule.id,
                                                    statusMap[state.attendanceBySchedule[schedule.id]?.status]
                                                        ?: AttendanceStatus.PRESENT
                                                )
                                                when (statusMap[state.attendanceBySchedule[schedule.id]?.status]
                                                    ?: AttendanceStatus.PRESENT) {
                                                    AttendanceStatus.PRESENT -> {
                                                        viewModel.updateAttendanceCount(
                                                            schedule.subjectId,
                                                            0
                                                        )
                                                        subjectMapLocal[schedule.subjectId]!!.present++
                                                    }

                                                    AttendanceStatus.ABSENT -> {
                                                        viewModel.updateAttendanceCount(
                                                            schedule.subjectId,
                                                            1
                                                        )
                                                        viewModel.updateAttendanceCount(
                                                            schedule.subjectId,
                                                            2
                                                        )
                                                        if (subjectMapLocal[schedule.subjectId]!!.present > 0)
                                                            subjectMapLocal[schedule.subjectId]!!.present--
                                                        subjectMapLocal[schedule.subjectId]!!.absent++
                                                    }

                                                    AttendanceStatus.UNMARKED -> {
                                                        viewModel.updateAttendanceCount(
                                                            schedule.subjectId,
                                                            3
                                                        )
                                                        if (subjectMapLocal[schedule.subjectId]!!.absent > 0)
                                                            subjectMapLocal[schedule.subjectId]!!.absent--
                                                    }
                                                }
                                            }
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                color = colorMap[state.attendanceBySchedule[schedule.id]?.status
                                                    ?: AttendanceStatus.UNMARKED] ?: Color.Black,
                                                shape = RoundedCornerShape(4.dp)
                                            )
                                    ) {
                                        Text(
                                            text = subjectMap[schedule.subjectId]?.name ?: "?",
                                            fontSize = 10.sp,
                                            maxLines = 2,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 14.sp,
                                            color = Color.White,
                                            modifier = Modifier.wrapContentSize()
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

        Row(modifier = Modifier.padding(horizontal = 4.dp)) {
            Spacer(modifier = Modifier.weight(.55f))
            Text("Present", modifier = Modifier.weight(.15f), fontSize = 12.sp, textAlign = TextAlign.Center)
            Text("Absent", modifier = Modifier.weight(.15f), fontSize = 12.sp, textAlign = TextAlign.Center)
            Text("Percent", modifier = Modifier.weight(.15f), fontSize = 12.sp, textAlign = TextAlign.Center)
        }
        subjectMapLocal.forEach { (_, subject) ->
            val percent = if (subject.absent == 0) 100 else subject.present * 100 / (subject.present + subject.absent)
            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(32.dp)
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                    .background(color = getAttendanceColor(percent.toFloat()), RoundedCornerShape(100))
            ) {
                Text(
                    subject.name,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .weight(.55f)
                        .fillMaxHeight()
                        .wrapContentHeight(Alignment.CenterVertically),
                    maxLines = 2,
                    lineHeight = 16.sp,
                )
                Text(
                    text = "${subject.present}",
                    fontSize = 14.sp,
                    modifier = Modifier
                        .weight(.15f)
                        .fillMaxHeight()
                        .wrapContentHeight(Alignment.CenterVertically),
                    textAlign = TextAlign.Center,
                )
                Text(
                    "${subject.absent}",
                    fontSize = 14.sp,
                    modifier = Modifier
                        .weight(.15f)
                        .fillMaxHeight()
                        .wrapContentHeight(Alignment.CenterVertically),
                    textAlign = TextAlign.Center,
                )
                Text(
                    "$percent %",
                    fontSize = 14.sp,
                    modifier = Modifier
                        .weight(.15f)
                        .fillMaxHeight()
                        .wrapContentHeight(Alignment.CenterVertically),
                    textAlign = TextAlign.Center,
                )
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
            .padding(horizontal = 8.dp, vertical = 24.dp)
            .fillMaxWidth()
            .background(color = Color.LightGray, shape = RoundedCornerShape(100)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        IconButton(onClick = onPreviousClick, modifier = Modifier.weight(.1f)) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, "Previous Week")
        }

        Text(
            text = getWeekRange(currentWeek),
            fontSize = 16.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(.7f)
        )

        IconButton(onClick = { showDatePicker = true }, modifier = Modifier.weight(.1f)) {
            Icon(Icons.Filled.DateRange, "Select Date")
        }
        IconButton(onClick = onNextClick, modifier = Modifier.weight(.1f)) {
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, "Next Week")
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

    calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    val monday = calendar.time

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
    val weekStart = calendar.clone() as Calendar

    weekStart.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    weekStart.set(Calendar.HOUR_OF_DAY, 0)
    weekStart.set(Calendar.MINUTE, 0)
    weekStart.set(Calendar.SECOND, 0)
    weekStart.set(Calendar.MILLISECOND, 0)

    return weekStart.timeInMillis
}

fun getAttendanceColor(value: Float): Color {
    val clamped = value.coerceIn(0f, 100f)
    return when {
        clamped == 0f -> Absent
        clamped < 37.5f -> lerp(Absent, Middle, clamped / 37.5f)
        clamped < 75f -> lerp(Middle, Present, (clamped - 37.5f) / 37.5f)
        else -> Present
    }
}
