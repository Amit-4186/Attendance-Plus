package com.example.attendanceplus.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.attendanceplus.Screen
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import com.example.attendanceplus.screens.TimetableState.Loading

@Composable
fun DecisionScreen(navController: NavController) {
    val setupViewModel: SetupViewModel = hiltViewModel()
    val isSetupComplete by setupViewModel.isSetupComplete.collectAsState(initial = false)

    LaunchedEffect(Unit) {
        if (isSetupComplete) {
            navController.navigate(Screen.Timetable.route) {
                popUpTo(Screen.Decision.route) { inclusive = true }
            }
        } else {
            navController.navigate(Screen.Subjects.route) {
                popUpTo(Screen.Decision.route) { inclusive = true }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }

}