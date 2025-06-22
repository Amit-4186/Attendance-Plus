package com.example.attendanceplus.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.unit.dp
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

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Add Subjects") })
        },
        floatingActionButton = {
            Row {
                FloatingActionButton(
                    onClick = { showDialog = true },
                    modifier = Modifier.padding(4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Add Subjects", modifier = Modifier.padding(start = 8.dp))
                        Icon(Icons.Filled.Add, "Add Subjects", modifier = Modifier.padding(end = 8.dp))
                    }
                }
                FloatingActionButton(
                    onClick = { navController.navigate(Screen.Schedule.route) },
                    modifier = Modifier.padding(4.dp)
                ) {
                    Row {
                        Text("Next", modifier = Modifier.padding(start = 8.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, "Next", modifier = Modifier.padding(end = 8.dp))
                    }
                }
            }
        }
    ) { padding ->

        if (showDialog) {
            Dialog(onDismissRequest = { showDialog = false }) {

                var newSubject by remember { mutableStateOf("") }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    OutlinedTextField(
                        value = newSubject,
                        onValueChange = { newSubject = it }
                    )
                    Row {
                        Button(onClick = { showDialog = false }) {
                            Text("Cancel")
                        }
                        Button(onClick = {
                            if (newSubject.isNotEmpty()) {
                                viewModel.addSubject(newSubject)
                            } else {
                                //TODO subject name cannot be empty
                            }
                            newSubject = ""
                        }) {
                            Text("Add Subject")
                        }
                    }
                }
            }
        }

        LazyColumn(modifier = Modifier.padding(padding)) {

            subjects.forEach { subject ->
                item {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(subject.name)
                        IconButton(onClick = { viewModel.deleteSubject(subject.id) }) {
                            Icon(Icons.Filled.Delete, "Delete")
                        }
                    }
                }
            }
        }
    }
}