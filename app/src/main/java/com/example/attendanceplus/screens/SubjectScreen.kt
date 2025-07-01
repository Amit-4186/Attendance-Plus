package com.example.attendanceplus.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.attendanceplus.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectsScreen(navController: NavController) {

    val viewModel: SetupViewModel = hiltViewModel()
    val subjects by viewModel.subjects.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

    var newSubject by remember { mutableStateOf("") }

    fun addSubject() {
        if (newSubject.isNotEmpty()) {
            newSubject = newSubject.trim()
            if (subjects.any { it.name.equals(newSubject, ignoreCase = true) }) {
                Toast.makeText(context, "Subject already exists!", Toast.LENGTH_SHORT).show()
            } else {
                viewModel.addSubject(newSubject)
                showDialog = false
                newSubject = ""
            }
        } else {
            Toast.makeText(context, "Subject name can not be blank!", Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(
                    "Add Subjects",
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )
            })
        },
        floatingActionButton = {
            Row {
                FloatingActionButton(
                    onClick = { showDialog = true },
                    modifier = Modifier.padding(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Add Subject", modifier = Modifier.padding(start = 8.dp))
                        Icon(
                            Icons.Filled.Add, "Add Subject", modifier = Modifier
                                .padding(end = 8.dp)
                                .size(16.dp)
                        )
                    }
                }
                FloatingActionButton(
                    onClick = {
                        if(subjects.isEmpty()){
                            Toast.makeText(context, "Add one or more subjects!", Toast.LENGTH_SHORT).show()
                        }
                        else{
                            navController.navigate(Screen.Schedule.route)
                        }
                    },
                    modifier = Modifier.padding(4.dp)
                ) {
                    Row {
                        Text("Next", modifier = Modifier.padding(start = 8.dp))
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowForward, "Next", modifier = Modifier
                                .padding(end = 8.dp)
                                .size(16.dp)
                        )
                    }
                }
            }
        }
    ) { padding ->

        if (showDialog) {
            Dialog(onDismissRequest = { showDialog = false }) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.background,
                            shape = RoundedCornerShape(16.dp)
                        )
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    OutlinedTextField(
                        value = newSubject,
                        onValueChange = { newSubject = sanitizeInput(it) },
                        modifier = Modifier
                            .padding(12.dp),
                        label = { Text("Subject Name") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions.Default.copy(
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { addSubject() }
                        )
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        modifier = Modifier
                            .padding(bottom = 12.dp)
                            .fillMaxWidth()
                    ) {
                        Button(onClick = { showDialog = false }) {
                            Text("Cancel")
                        }
                        Button(onClick = { addSubject() }) {
                            Text("Add Subject")
                        }
                    }
                }
            }
        }

        if (subjects.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "No Subjects Added")
            }
        }
        LazyColumn(modifier = Modifier.padding(padding)) {
            subjects.forEach { subject ->
                item {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                            .background(
                                MaterialTheme.colorScheme.primary,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clip(RoundedCornerShape(8.dp))
                            .height(60.dp)
                    ) {
                        Text(
                            subject.name,
                            modifier = Modifier.padding(start = 12.dp),
                            fontSize = 20.sp,
                            color = Color.White
                        )
                        IconButton(onClick = { viewModel.deleteSubject(subject.id) }) {
                            Icon(Icons.Filled.Delete, "Delete", tint = Color.White)
                        }
                    }
                }
            }
        }
    }
}

fun sanitizeInput(input: String): String {
    var result = input.trimStart()
    result = result.replace(Regex("\\s{2,}"), " ")
    return result
}
