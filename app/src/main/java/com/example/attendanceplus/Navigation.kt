package com.example.attendanceplus

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.attendanceplus.screens.DecisionScreen
import com.example.attendanceplus.screens.ScheduleScreen
import com.example.attendanceplus.screens.SubjectsScreen
import com.example.attendanceplus.screens.TimetableScreen

sealed class Screen(val route: String) {
    object Decision : Screen("decision")
    object Subjects : Screen("subjects")
    object Schedule : Screen("schedule")
    object Timetable : Screen("timetable")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

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