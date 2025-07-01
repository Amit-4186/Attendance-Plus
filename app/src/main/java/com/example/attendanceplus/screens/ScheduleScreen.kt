package com.example.attendanceplus.screens

import android.widget.Toast
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    val context = LocalContext.current

    val mon = remember { mutableStateListOf<Subject>() }
    val tue = remember { mutableStateListOf<Subject>() }
    val wed = remember { mutableStateListOf<Subject>() }
    val thu = remember { mutableStateListOf<Subject>() }
    val fri = remember { mutableStateListOf<Subject>() }
    val sat = remember { mutableStateListOf<Subject>() }

    val weekMap = mapOf(
        2 to "Mon", 3 to "Tue", 4 to "Wed",
        5 to "Thu", 6 to "Fri", 7 to "Sat"
    )

    val weekListMap = mapOf<Int, MutableList<Subject>>(
        2 to mon, 3 to tue, 4 to wed,
        5 to thu, 6 to fri, 7 to sat
    )

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Create Schedule", fontWeight = FontWeight.Bold) })
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (!weekListMap.values.all { it.isNotEmpty() }) {
                        Toast.makeText(context, "Complete Schedule!", Toast.LENGTH_SHORT).show()
                    } else {
                        viewModel.completeSetup()
                        navController.navigate(Screen.Timetable.route) {
                            popUpTo(Screen.Subjects.route) { inclusive = true }
                        }
                    }
                }
            ) {
                Row {
                    Row {
                        Text("Finish", modifier = Modifier.padding(start = 8.dp), fontSize = 16.sp)
                        Icon(
                            Icons.Filled.Done,
                            "Finish",
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 8.dp)
                        )
                    }
                }
            }
        }
    ) { padding ->

        Row(
            modifier = Modifier
                .fillMaxHeight()
                .padding(padding)
        ) {
            Column {
                for (day in 2..7) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .padding(start = 4.dp, top = 1.dp, bottom = 1.dp)
                            .height(52.dp)
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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
            ) {
                for (day in 2..7) {
                    Row(
                        modifier = Modifier
                            .padding(1.dp)
                            .height(52.dp)
                    ) {
                        weekListMap[day]!!.forEach { subject ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .padding(horizontal = 1.dp)
                                    .fillMaxHeight()
                                    .width(80.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                            ) {
                                Text(
                                    text = subject.name,
                                    fontSize = 12.sp,
                                    textAlign = TextAlign.Center,
                                    lineHeight = 16.sp,
                                    modifier = Modifier.wrapContentSize()
                                )
                            }
                        }

                        var expanded by remember { mutableStateOf(false) }
                        var xOffset by remember { mutableIntStateOf(0) }
                        var xOffsetFrozen by remember { mutableStateOf<Int?>(null) }

                        IconButton(
                            onClick = {
                                xOffsetFrozen = xOffset
                                expanded = true
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = Color(
                                    0x26000000
                                )
                            ),
                            modifier = Modifier
                                .height(60.dp)
                                .width(40.dp)
                                .padding(end = 2.dp)
                                .onGloballyPositioned { layoutCoordinates ->
                                    xOffset = layoutCoordinates.positionInRoot().x.toInt()
                                }
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Add subject to schedule",
                                modifier = Modifier.size(18.dp),
                                tint = Color(0x80000000)
                            )
                        }

                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = {
                                expanded = false
                                xOffsetFrozen = null
                            },
                            offset = with(LocalDensity.current) {
                                val dropdownWidthDp = 200.dp
                                val rawPx = xOffsetFrozen ?: xOffset
                                val rawDp = rawPx.toDp()
                                val maxXDp = (LocalWindowInfo.current.containerSize.width.toDp() - dropdownWidthDp)
                                    .coerceAtLeast(0.dp)
                                DpOffset(x = rawDp.coerceAtMost(maxXDp), y = 0.dp)
                            }
                        ) {
                            subjects.forEach { subject ->
                                DropdownMenuItem(
                                    text = { Text(subject.name) },
                                    onClick = {
                                        expanded = false
                                        weekListMap[day]!!.add(subject)
                                        viewModel.addScheduleEntry(day, subject.id, weekListMap[day]!!.size)
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