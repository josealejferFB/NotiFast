package com.example.notifast

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.notifast.model.Note
import com.example.notifast.ui.AddNoteScreen
import com.example.notifast.ui.EditNoteScreen
import com.example.notifast.ui.LoginScreen
import com.example.notifast.ui.NotesScreen
import com.example.notifast.ui.theme.NotiFastTheme
import com.example.notifast.viewmodel.AuthViewModel
import com.example.notifast.viewmodel.NotesViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NotiFastTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = viewModel()
                val notesViewModel: NotesViewModel = viewModel()
                
                // Variable temporal para guardar la nota que vamos a editar
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
                            onEditNote = { note -> 
                                selectedNote = note
                                navController.navigate("edit_note")
                            },
                            onLogout = {
                                navController.navigate("login") {
                                    popUpTo("notes") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("add_note") {
                        AddNoteScreen(
                            viewModel = notesViewModel,
                            onNoteAdded = { navController.popBackStack() }
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
                }
            }
        }
    }
}
