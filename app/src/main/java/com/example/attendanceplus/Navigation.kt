package com.example.attendanceplus

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.attendanceplus.screens.DecisionScreen
import com.example.attendanceplus.screens.ScheduleScreen
import com.example.attendanceplus.screens.SetupViewModel
import com.example.attendanceplus.screens.SubjectsScreen
import com.example.attendanceplus.screens.TimetableScreen

sealed class Screen(val route: String) {
    object Decision : Screen("decision")
    object Subjects : Screen("subjects")
    object Schedule : Screen("schedule")
    object Timetable : Screen("timetable")
}

@Composable
fun AppNavigation(contentPadding : PaddingValues) {
    val navController = rememberNavController()

    val setupViewModel: SetupViewModel = hiltViewModel()
    val isSetupComplete by setupViewModel.isSetupComplete.collectAsState(initial = false)

    NavHost(
        navController = navController,
        startDestination = Screen.Decision.route
    ) {
        composable(Screen.Decision.route) {
            DecisionScreen(navController)
        }
        composable(Screen.Subjects.route) {
            SubjectsScreen(navController)
        }
        composable(Screen.Schedule.route) {
            ScheduleScreen(navController)
        }
        composable(Screen.Timetable.route) {
            TimetableScreen()
        }
    }
}