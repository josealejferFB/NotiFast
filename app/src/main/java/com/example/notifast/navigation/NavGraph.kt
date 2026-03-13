package com.example.notifast.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.notifast.model.Evaluation
import com.example.notifast.model.Note
import com.example.notifast.ui.*
import com.example.notifast.viewmodel.AuthViewModel
import com.example.notifast.viewmodel.NotesViewModel

@Composable
fun NavGraph(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    notesViewModel: NotesViewModel
) {
    var selectedNote by remember { mutableStateOf<Note?>(null) }
    var selectedEvaluation by remember { mutableStateOf<Evaluation?>(null) }

    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onLoginSuccess = {
                    navController.navigate(Screen.Notes.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        // --- PANTALLAS PRINCIPALES CON HOTBAR ---
        composable(Screen.Notes.route) {
            NotesScreen(
                notesViewModel = notesViewModel,
                authViewModel = authViewModel,
                onAddNote = { navController.navigate(Screen.AddNote.route) },
                onAddSubject = { navController.navigate(Screen.AddSubject.route) },
                onEditNote = { note ->
                    selectedNote = note
                    navController.navigate(Screen.EditNote.route)
                },
                onEditProfile = {
                    navController.navigate(Screen.EditProfile.route)
                },
                onSubjectClick = { subjectName ->
                    navController.navigate(Screen.SubjectDetail.createRoute(subjectName))
                },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Notes.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Calendar.route) {
            CalendarScreen(
                viewModel = notesViewModel,
                onAddEvaluation = { navController.navigate(Screen.AddEvaluation.route) },
                onEditEvaluation = { eval ->
                    selectedEvaluation = eval
                    navController.navigate(Screen.EditEvaluation.route)
                }
            )
        }
        
        composable(Screen.Schedules.route) {
            SchedulesScreen()
        }
        
        composable(Screen.Grades.route) {
            GradesScreen(viewModel = notesViewModel)
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(
                authViewModel = authViewModel,
                onEditProfile = { navController.navigate(Screen.EditProfile.route) },
                onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Notes.route) { inclusive = true }
                    }
                }
            )
        }

        // --- PANTALLAS SECUNDARIAS ---
        composable(Screen.EditProfile.route) {
            EditProfileScreen(
                viewModel = authViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.AddNote.route) {
            AddNoteScreen(
                viewModel = notesViewModel,
                onNoteAdded = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.AddEvaluation.route) {
            AddEvaluationScreen(
                viewModel = notesViewModel,
                onEvaluationAdded = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.EditEvaluation.route) {
            selectedEvaluation?.let { eval ->
                EditEvaluationScreen(
                    evaluation = eval,
                    viewModel = notesViewModel,
                    onEvaluationUpdated = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable(Screen.AddSubject.route) {
            AddSubjectScreen(
                viewModel = notesViewModel,
                onSubjectAdded = { navController.popBackStack() },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Screen.EditNote.route) {
            selectedNote?.let { note ->
                EditNoteScreen(
                    note = note,
                    viewModel = notesViewModel,
                    onNoteUpdated = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }
        }
        composable(
            route = Screen.SubjectDetail.route,
            arguments = listOf(navArgument("subjectName") { type = NavType.StringType })
        ) { backStackEntry ->
            val subjectName = backStackEntry.arguments?.getString("subjectName") ?: ""
            SubjectDetailScreen(
                subjectName = subjectName,
                viewModel = notesViewModel,
                onEditNote = { note ->
                    selectedNote = note
                    navController.navigate(Screen.EditNote.route)
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
