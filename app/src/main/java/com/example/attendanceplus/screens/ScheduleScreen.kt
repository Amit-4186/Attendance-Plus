package com.example.attendanceplus.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.attendanceplus.Screen
import com.example.attendanceplus.database.Subject


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(navController: NavController) {
    val viewModel: SetupViewModel = hiltViewModel()
    val subjects by viewModel.subjects.collectAsState()
    val schedule by viewModel.schedule.collectAsState()

    val mon = remember { mutableStateListOf<Subject>() }
    val tue = remember { mutableStateListOf<Subject>() }
    val wed = remember { mutableStateListOf<Subject>() }
    val thu = remember { mutableStateListOf<Subject>() }
    val fri = remember { mutableStateListOf<Subject>() }
    val sat = remember { mutableStateListOf<Subject>() }

    val weekMap = mapOf<Int, String>(
        2 to "Mon",
        3 to "Tue",
        4 to "Wed",
        5 to "Thu",
        6 to "Fri",
        7 to "Sat"
    )

    val weekListMap = mapOf<Int, MutableList<Subject>>(
        2 to mon,
        3 to tue,
        4 to wed,
        5 to thu,
        6 to fri,
        7 to sat
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Create Schedule") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                viewModel.completeSetup()
                navController.navigate(Screen.Timetable.route) {
                    popUpTo(Screen.Subjects.route) { inclusive = true }
                }
            }) {
                Row {
                    Row {
                        Text("Finish", modifier = Modifier.padding(start = 8.dp), fontSize = 16.sp)
                        Icon(Icons.Filled.Done, "Finish", modifier = Modifier.size(24.dp).padding(end = 8.dp))
                    }
                }
            }
        }
    ) { padding ->

        Row(
            modifier = Modifier
                .wrapContentHeight()
                .padding(padding)
        ) {
            Column {
                for (day in 2..7) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .padding(horizontal = 1.dp, vertical = 1.dp)
                            .height(60.dp)
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
                    Row(
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .heightIn(60.dp)
                            .padding(horizontal = 2.dp, vertical = 1.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        weekListMap[day]!!.forEach { subject ->
                            Text(
                                subject.name,
                                modifier = Modifier
                                    .size(height = 50.dp, width = 70.dp)
                                    .align(Alignment.CenterVertically)
                                    .background(color = Color.Cyan)
                                    .clip(RoundedCornerShape(8.dp)),
                            )
                        }

                        var expanded by remember { mutableStateOf(false) }
                        var xCord by remember { mutableStateOf(0.dp) }
                        IconButton(
                            onClick = {
                                expanded = true
                                xCord = (52 + schedule.size * 88).dp
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color(0x26000000)
                            ),
                            modifier = Modifier
                                .height(60.dp)
                                .width(40.dp)
                                .padding(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add subject to schedule",
                                modifier = Modifier.size(18.dp),
                                tint = Color(0x80000000)
                            )
                        }

                        // Dropdown menu for adding subjects
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false },
                            offset = DpOffset(xCord, 0.dp)
                        ) {
                            subjects.forEach { subject ->
                                DropdownMenuItem(
                                    text = { Text(subject.name) },
                                    onClick = {
                                        weekListMap[day]!!.add(subject)
                                        viewModel.addScheduleEntry(
                                            day,
                                            subject.id,
                                            weekListMap[day]!!.size
                                        )
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}