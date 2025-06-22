package com.example.attendanceplus.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.attendanceplus.Screen
import androidx.compose.runtime.getValue

@Composable
fun DecisionScreen(navController: NavController) {
    val setupViewModel: SetupViewModel = hiltViewModel()
    val isSetupComplete by setupViewModel.isSetupComplete.collectAsState(initial = false)

    // Check setup state only once
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

    // Empty content - no UI flicker
    Box(modifier = Modifier.fillMaxSize()) {
        Text("Loading")
    }
}