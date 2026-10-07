package com.notifast.app.presentation.navigation

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String = "",
    val icon: ImageVector? = null
) {
    data object Onboarding : Screen("onboarding")
    data object Login : Screen("login")

    data object Notes : Screen("notes", "Apuntes", Icons.AutoMirrored.Filled.MenuBook)
    data object NoteEditor : Screen("note_editor?subject={subject}&noteId={noteId}") {
        fun createRoute(subject: String? = null, noteId: String? = null): String {
            val params = mutableListOf<String>()
            subject?.let { params.add("subject=${Uri.encode(it)}") }
            noteId?.let { params.add("noteId=${Uri.encode(it)}") }
            return if (params.isEmpty()) "note_editor" else "note_editor?${params.joinToString("&")}"
        }
    }
    data object AddSubject : Screen("add_subject")

    data object Schedules : Screen("agenda", "Agenda", Icons.Default.Schedule)
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
    data object EditSchedule : Screen("edit_schedule/{scheduleId}") {
        fun createRoute(scheduleId: String) = "edit_schedule/${Uri.encode(scheduleId)}"
    }

    data object EditProfile : Screen("edit_profile")

    companion object {
        /**
         * Lazy getter: a `val` initialized inside the companion of a sealed class
         * that references its own `data object` subclasses can capture them before
         * their init finishes (empty routes / null icons). A `get()` defers lookup.
         */
        val bottomNavItems: List<Screen>
            get() = listOf(Notes, Schedules, Grades, Settings)
    }
}
