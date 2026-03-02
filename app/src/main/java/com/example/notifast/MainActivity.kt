package com.example.notifast

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.notifast.model.Note
import com.example.notifast.ui.*
import com.example.notifast.ui.theme.NotiFastTheme
import com.example.notifast.ui.theme.Ruby
import com.example.notifast.viewmodel.AuthViewModel
import com.example.notifast.viewmodel.NotesViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NotiFastTheme {
                // DEGRADADO PROFESIONAL: Blanco -> Ruby suave
                val backgroundGradient = Brush.verticalGradient(
                    colors = listOf(
                        Color.White,
                        Ruby.copy(alpha = 0.15f) // Ruby con transparencia para elegancia
                    )
                )

                Box(modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundGradient)
                ) {
                    val navController = rememberNavController()
                    val authViewModel: AuthViewModel = viewModel()
                    val notesViewModel: NotesViewModel = viewModel()
                    
                    var selectedNote by remember { mutableStateOf<Note?>(null) }

                    NavHost(navController = navController, startDestination = "login") {
                        composable("login") {
                            LoginScreen(
                                viewModel = authViewModel,
                                onLoginSuccess = {
                                    navController.navigate("notes") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("notes") {
                            NotesScreen(
                                notesViewModel = notesViewModel,
                                authViewModel = authViewModel,
                                onAddNote = { navController.navigate("add_note") },
                                onAddSubject = { navController.navigate("add_subject") },
                                onEditNote = { note -> 
                                    selectedNote = note
                                    navController.navigate("edit_note")
                                },
                                onEditProfile = { 
                                    navController.navigate("edit_profile")
                                },
                                onSubjectClick = { subjectName ->
                                    navController.navigate("subject_detail/$subjectName")
                                },
                                onLogout = {
                                    navController.navigate("login") {
                                        popUpTo("notes") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("edit_profile") { 
                            EditProfileScreen(
                                viewModel = authViewModel,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("add_note") {
                            AddNoteScreen(
                                viewModel = notesViewModel,
                                onNoteAdded = { navController.popBackStack() },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("add_subject") {
                            AddSubjectScreen(
                                viewModel = notesViewModel,
                                onSubjectAdded = { navController.popBackStack() },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("edit_note") {
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
                            route = "subject_detail/{subjectName}",
                            arguments = listOf(navArgument("subjectName") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val subjectName = backStackEntry.arguments?.getString("subjectName") ?: ""
                            SubjectDetailScreen(
                                subjectName = subjectName,
                                viewModel = notesViewModel,
                                onEditNote = { note ->
                                    selectedNote = note
                                    navController.navigate("edit_note")
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}
