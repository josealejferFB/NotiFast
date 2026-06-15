package com.example.notifast.presentation.navigation

import androidx.compose.foundation.layout.padding
import com.example.notifast.presentation.feature.auth.LoginScreen
import com.example.notifast.presentation.feature.notes.NotesScreen
import com.example.notifast.presentation.feature.addnote.AddNoteScreen
import com.example.notifast.presentation.feature.editnote.EditNoteScreen
import com.example.notifast.presentation.feature.subjectdetail.SubjectDetailScreen
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

@Composable
fun MainContent() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = Screen.bottomNavItems.any { screen ->
        currentDestination?.hierarchy?.any { it.route == screen.route } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    Screen.bottomNavItems.forEach { screen ->
                        NavigationBarItem(
                            icon = {
                                screen.icon?.let {
                                    Icon(it, contentDescription = screen.title)
                                }
                            },
                            label = { Text(screen.title) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(
                    onLoginSuccess = {
                        navController.navigate(Screen.Notes.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    },
                    onSignUpSuccess = {
                        navController.navigate(Screen.Notes.route) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.Notes.route) {
                NotesScreen(
                    onNoteClick = { note -> navController.navigate(Screen.EditNote.createRoute(note.id)) },
                    onAddNoteClick = { navController.navigate(Screen.AddNote.route) },
                    onSubjectClick = { subject -> navController.navigate(Screen.SubjectDetail.createRoute(subject)) }
                )
            }
            composable(Screen.AddNote.route) {
                AddNoteScreen(
                    onBackClick = { navController.popBackStack() },
                    onNoteAdded = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.EditNote.route,
                arguments = listOf(navArgument("noteId") { type = NavType.StringType })
            ) {
                EditNoteScreen(
                    onBackClick = { navController.popBackStack() },
                    onNoteSaved = { navController.popBackStack() }
                )
            }
            composable(Screen.AddSubject.route) {
                // Will be implemented in Task 9
            }
            composable(
                route = Screen.SubjectDetail.route,
                arguments = listOf(navArgument("subjectName") { type = NavType.StringType })
            ) {
                SubjectDetailScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.Calendar.route) {
                // Will be implemented in Task 13
            }
            composable(Screen.Schedules.route) {
                // Will be implemented in Task 14
            }
            composable(Screen.Grades.route) {
                // Will be implemented in Task 15
            }
            composable(Screen.Settings.route) {
                // Will be implemented in Task 16
            }
            composable(Screen.EditProfile.route) {
                // Will be implemented in Task 17
            }
            composable(Screen.AddEvaluation.route) {
                // Will be implemented in Task 18
            }
            composable(
                route = Screen.EditEvaluation.route,
                arguments = listOf(navArgument("evalId") { type = NavType.StringType })
            ) {
                // Will be implemented in Task 18
            }
        }
    }
}
