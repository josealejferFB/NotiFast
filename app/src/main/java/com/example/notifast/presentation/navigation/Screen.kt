package com.example.notifast.presentation.navigation

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String = "",
    val icon: ImageVector? = null
) {
    data object Login : Screen("login")

    data object Notes : Screen("notes", "Apuntes", Icons.Default.MenuBook)
    data object AddNote : Screen("add_note?subject={subject}") {
        fun createRoute(subject: String? = null) =
            if (subject != null) "add_note?subject=${Uri.encode(subject)}" else "add_note"
    }
    data object EditNote : Screen("edit_note/{noteId}") {
        fun createRoute(noteId: String) = "edit_note/${Uri.encode(noteId)}"
    }
    data object AddSubject : Screen("add_subject")

    data object Calendar : Screen("calendar", "Calendario", Icons.Default.CalendarMonth)
    data object Schedules : Screen("horarios", "Horarios", Icons.Default.Schedule)
    data object Grades : Screen("calificaciones", "Calificaciones", Icons.Default.Grade)
    data object Settings : Screen("settings", "Ajustes", Icons.Default.Settings)

    data object AddEvaluation : Screen("add_evaluation?subject={subject}") {
        fun createRoute(subject: String? = null) =
            if (subject != null) "add_evaluation?subject=${Uri.encode(subject)}" else "add_evaluation"
    }
    data object EditEvaluation : Screen("edit_evaluation/{evalId}") {
        fun createRoute(evalId: String) = "edit_evaluation/${Uri.encode(evalId)}"
    }

    data object SubjectDetail : Screen("subject_detail/{subjectName}") {
        fun createRoute(subjectName: String) = "subject_detail/${Uri.encode(subjectName)}"
    }

    data object AddSchedule : Screen("add_schedule")

    data object EditProfile : Screen("edit_profile")

    companion object {
        val bottomNavItems = listOf(Notes, Calendar, Schedules, Grades, Settings)
    }
}
