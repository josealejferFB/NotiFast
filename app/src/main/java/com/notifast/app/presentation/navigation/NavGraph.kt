package com.notifast.app.presentation.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.notifast.app.presentation.atoms.NotiFastBottomNav
import com.notifast.app.presentation.atoms.NotiFastStudyBackground
import com.notifast.app.presentation.feature.addnote.AddNoteScreen
import com.notifast.app.presentation.feature.addsubject.AddSubjectScreen
import com.notifast.app.presentation.feature.addevaluation.AddEvaluationScreen
import com.notifast.app.presentation.feature.auth.LoginScreen
import com.notifast.app.presentation.feature.editnote.EditNoteScreen
import com.notifast.app.presentation.feature.editevaluation.EditEvaluationScreen
import com.notifast.app.presentation.feature.editprofile.EditProfileScreen
import com.notifast.app.presentation.feature.grades.GradesScreen
import com.notifast.app.presentation.feature.main.MainViewModel
import com.notifast.app.presentation.feature.notes.NotesScreen
import com.notifast.app.presentation.feature.schedules.SchedulesScreen
import com.notifast.app.presentation.feature.settings.SettingsScreen
import com.notifast.app.presentation.feature.subjectdetail.SubjectDetailScreen
import com.notifast.app.presentation.feature.addschedule.AddScheduleScreen
import com.notifast.app.presentation.feature.editschedule.EditScheduleScreen
import com.notifast.app.presentation.theme.color.Ruby

@Composable
fun MainContent() {
    val mainViewModel: MainViewModel = hiltViewModel()
    val authState by mainViewModel.authState.collectAsStateWithLifecycle()

    if (authState == null) {
        Box(modifier = Modifier.fillMaxSize()) {
            NotiFastStudyBackground()
            CircularProgressIndicator(color = Ruby, modifier = Modifier.align(Alignment.Center))
        }
        return
    }

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    LaunchedEffect(authState) {
        if (authState == true) {
            navController.navigate(Screen.Notes.route) {
                popUpTo(0) { inclusive = true }
            }
        }
    }

    val showBottomBar = Screen.bottomNavItems.any { screen ->
        currentDestination?.hierarchy?.any { it.route == screen.route } == true
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NotiFastBottomNav(
                    items = Screen.bottomNavItems,
                    currentRoute = currentDestination?.route,
                    onItemClick = { screen ->
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
    ) {
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route
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
                    onAddNoteClick = { subject -> navController.navigate(Screen.AddNote.createRoute(subject)) },
                    onAddSubjectClick = { navController.navigate(Screen.AddSubject.route) },
                    onSubjectClick = { subject -> navController.navigate(Screen.SubjectDetail.createRoute(subject)) },
                    onNoteClick = { noteId -> navController.navigate(Screen.EditNote.createRoute(noteId)) },
                    onAddEvaluationClick = { subject -> navController.navigate(Screen.AddEvaluation.createRoute(subject)) },
                    onSubjectDetailClick = { subject -> navController.navigate(Screen.SubjectDetail.createRoute(subject)) },
                    onEditProfile = { navController.navigate(Screen.EditProfile.route) }
                )
            }
            composable(
                route = Screen.AddNote.route,
                arguments = listOf(navArgument("subject") { type = NavType.StringType; defaultValue = "" })
            ) { backStackEntry ->
                val subject = Uri.decode(backStackEntry.arguments?.getString("subject") ?: "")
                AddNoteScreen(
                    subject = subject,
                    onBackClick = { navController.popBackStack() },
                    onNoteAdded = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.EditNote.route,
                arguments = listOf(navArgument("noteId") { type = NavType.StringType }),
                enterTransition = {
                    androidx.compose.animation.slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.8f, stiffness = 400f)
                    ) + androidx.compose.animation.fadeIn()
                },
                exitTransition = {
                    androidx.compose.animation.slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = androidx.compose.animation.core.spring(dampingRatio = 0.8f, stiffness = 400f)
                    ) + androidx.compose.animation.fadeOut()
                }
            ) {
                EditNoteScreen(
                    onBackClick = { navController.popBackStack() },
                    onNoteSaved = { navController.popBackStack() }
                )
            }
            composable(Screen.AddSubject.route) {
                AddSubjectScreen(
                    onSubjectCreated = { name ->
                        navController.navigate(Screen.SubjectDetail.createRoute(name)) {
                            popUpTo(Screen.Notes.route)
                        }
                    },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.SubjectDetail.route,
                arguments = listOf(navArgument("subjectName") { type = NavType.StringType })
            ) { backStackEntry ->
                val subjectName = Uri.decode(backStackEntry.arguments?.getString("subjectName") ?: "")
                SubjectDetailScreen(
                    onAddNoteClick = { navController.navigate(Screen.AddNote.createRoute(subjectName)) },
                    onAddEvaluationClick = { navController.navigate(Screen.AddEvaluation.createRoute(subjectName)) },
                    onBackClick = { navController.popBackStack() }
                )
            }
            composable(Screen.Schedules.route) {
                SchedulesScreen(
                    onAddScheduleClick = { navController.navigate(Screen.AddSchedule.route) },
                    onAddEvaluationClick = { navController.navigate(Screen.AddEvaluation.createRoute()) },
                    onTakeNoteClick = { subject -> navController.navigate(Screen.AddNote.createRoute(subject)) },
                    onEditScheduleClick = { scheduleId -> navController.navigate(Screen.EditSchedule.createRoute(scheduleId)) },
                    onEditEvaluationClick = { eval -> navController.navigate(Screen.EditEvaluation.createRoute(eval.id)) },
                    onEditProfile = { navController.navigate(Screen.EditProfile.route) }
                )
            }
            composable(Screen.AddSchedule.route) {
                AddScheduleScreen(
                    onBackClick = { navController.popBackStack() },
                    onScheduleAdded = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.EditSchedule.route,
                arguments = listOf(navArgument("scheduleId") { type = NavType.StringType })
            ) {
                EditScheduleScreen(
                    onBackClick = { navController.popBackStack() },
                    onScheduleAdded = { navController.popBackStack() }
                )
            }
            composable(Screen.Grades.route) {
                GradesScreen(
                    onEditProfile = { navController.navigate(Screen.EditProfile.route) }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onEditProfile = { navController.navigate(Screen.EditProfile.route) },
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(Screen.EditProfile.route) {
                EditProfileScreen(
                    onBack = { navController.popBackStack() },
                    onLogout = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0) { inclusive = true }
                        }
                    }
                )
            }
            composable(
                route = Screen.AddEvaluation.route,
                arguments = listOf(navArgument("subject") { type = NavType.StringType; defaultValue = "" })
            ) { backStackEntry ->
                val subject = Uri.decode(backStackEntry.arguments?.getString("subject") ?: "")
                AddEvaluationScreen(
                    subject = subject,
                    onEvaluationAdded = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(
                route = Screen.EditEvaluation.route,
                arguments = listOf(navArgument("evalId") { type = NavType.StringType })
            ) {
                EditEvaluationScreen(
                    onEvaluationUpdated = { navController.popBackStack() },
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}
