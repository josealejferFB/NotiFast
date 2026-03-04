package com.example.notifast.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String? = null, val icon: ImageVector? = null) {
    object Login : Screen("login")
    object Notes : Screen("notes", "Notas", Icons.Default.Description)
    object Calendar : Screen("calendar", "Calendario", Icons.Default.CalendarMonth)
    object Schedules : Screen("schedules", "Horarios", Icons.Default.EventNote)
    object Grades : Screen("grades", "Calificaciones", Icons.AutoMirrored.Filled.Assignment)
    object Settings : Screen("settings", "Ajustes", Icons.Default.Settings)
    
    object AddNote : Screen("add_note")
    object AddEvaluation : Screen("add_evaluation")
    object EditEvaluation : Screen("edit_evaluation") // NUEVO
    object AddSubject : Screen("add_subject")
    object EditNote : Screen("edit_note")
    object EditProfile : Screen("edit_profile")
    object SubjectDetail : Screen("subject_detail/{subjectName}") {
        fun createRoute(subjectName: String) = "subject_detail/$subjectName"
    }

    companion object {
        val bottomNavItems = listOf(Notes, Calendar, Schedules, Grades, Settings)
    }
}
