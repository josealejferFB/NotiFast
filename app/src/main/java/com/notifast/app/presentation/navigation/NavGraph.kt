package com.notifast.app.presentation.navigation

import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.notifast.app.presentation.atoms.NotiFastBottomNav
import com.notifast.app.presentation.atoms.NotiFastStudyBackground
import com.notifast.app.presentation.feature.addevaluation.AddEvaluationScreen
import com.notifast.app.presentation.feature.addevaluation.EvaluationEditorBottomSheet
import com.notifast.app.presentation.feature.addschedule.AddScheduleScreen
import com.notifast.app.presentation.feature.addschedule.ScheduleEditorBottomSheet
import com.notifast.app.presentation.feature.addsubject.AddSubjectBottomSheet
import com.notifast.app.presentation.feature.addsubject.AddSubjectScreen
import com.notifast.app.presentation.feature.editevaluation.EditEvaluationScreen
import com.notifast.app.presentation.feature.editprofile.EditProfileScreen
import com.notifast.app.presentation.feature.editschedule.EditScheduleScreen
import com.notifast.app.presentation.feature.grades.GradesScreen
import com.notifast.app.presentation.feature.main.MainViewModel
import com.notifast.app.presentation.feature.noteeditor.NoteEditorBottomSheet
import com.notifast.app.presentation.feature.notes.NotesScreen
import com.notifast.app.presentation.feature.onboarding.OnboardingScreen
import com.notifast.app.presentation.feature.schedules.SchedulesScreen
import com.notifast.app.presentation.feature.search.GlobalSearchBottomSheet
import com.notifast.app.presentation.feature.settings.SettingsScreen
import com.notifast.app.presentation.feature.subjectdetail.SubjectDetailScreen
import com.notifast.app.presentation.theme.color.Ruby

@OptIn(ExperimentalMaterial3Api::class)
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
    var showGlobalSearchSheet by remember { mutableStateOf(false) }
    var activeNoteEditorArgs by remember { mutableStateOf<Pair<String?, String?>?>(null) }
    var activeScheduleEditorId by remember { mutableStateOf<String?>(null) }
    var activeEvaluationEditorArgs by remember { mutableStateOf<Pair<String?, String?>?>(null) }
    var showAddSubjectSheetFromEditor by remember { mutableStateOf(false) }
    var pendingSubjectTarget by remember { mutableStateOf<String?>(null) }

    val startDestination = if (authState == true) Screen.Notes.route else Screen.Onboarding.route

    LaunchedEffect(authState) {
        val currentRoute = navController.currentDestination?.route
        when {
            authState == true && currentRoute != null && currentRoute != Screen.Notes.route -> {
                navController.navigate(Screen.Notes.route) { popUpTo(0) { inclusive = true } }
            }
            authState == false && currentRoute != null && currentRoute != Screen.Onboarding.route -> {
                navController.navigate(Screen.Onboarding.route) { popUpTo(0) { inclusive = true } }
            }
        }
    }

    val bottomNavRoutes = Screen.bottomNavItems.map { it.route }
    val currentRoute = currentDestination?.route
    val showBottomBar = bottomNavRoutes.any { tabRoute ->
        currentRoute == tabRoute || currentRoute?.startsWith("$tabRoute/") == true
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            if (showBottomBar) {
                NotiFastBottomNav(
                    items = Screen.bottomNavItems,
                    currentRoute = currentRoute,
                    onItemClick = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { _ ->
        NavHost(navController = navController, startDestination = startDestination) {
            composable(Screen.Onboarding.route) {
                OnboardingScreen(onOnboardingComplete = {
                    navController.navigate(Screen.Notes.route) { popUpTo(0) { inclusive = true } }
                })
            }
            composable(Screen.Notes.route) {
                NotesScreen(
                    onAddNoteClick = { subject -> activeNoteEditorArgs = Pair(null, subject) },
                    onAddSubjectClick = { navController.navigate(Screen.AddSubject.route) },
                    onSubjectClick = { subject -> navController.navigate(Screen.SubjectDetail.createRoute(subject)) },
                    onNoteClick = { noteId -> activeNoteEditorArgs = Pair(noteId, null) },
                    onAddEvaluationClick = { subject -> activeEvaluationEditorArgs = Pair(null, subject) },
                    onSubjectDetailClick = { subject -> navController.navigate(Screen.SubjectDetail.createRoute(subject)) },
                    onEditProfile = { navController.navigate(Screen.EditProfile.route) },
                    onSearchClick = { showGlobalSearchSheet = true }
                )
            }
            composable(Screen.AddSubject.route) {
                AddSubjectScreen(
                    onSubjectCreated = { name ->
                        navController.navigate(Screen.SubjectDetail.createRoute(name)) { popUpTo(Screen.Notes.route) }
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
                    onAddNoteClick = { activeNoteEditorArgs = Pair(null, subjectName) },
                    onAddEvaluationClick = { activeEvaluationEditorArgs = Pair(null, subjectName) },
                    onAddScheduleClick = { activeScheduleEditorId = "NEW" },
                    onBackClick = { navController.popBackStack() },
                    onNoteClick = { note -> activeNoteEditorArgs = Pair(note.id, null) }
                )
            }
            composable(Screen.Schedules.route) {
                SchedulesScreen(
                    onAddScheduleClick = { activeScheduleEditorId = "NEW" },
                    onAddEvaluationClick = { activeEvaluationEditorArgs = Pair(null, null) },
                    onTakeNoteClick = { subject -> activeNoteEditorArgs = Pair(null, subject) },
                    onEditScheduleClick = { scheduleId -> activeScheduleEditorId = scheduleId },
                    onEditEvaluationClick = { eval -> activeEvaluationEditorArgs = Pair(eval.id, null) },
                    onEditProfile = { navController.navigate(Screen.EditProfile.route) },
                    onSearchClick = { showGlobalSearchSheet = true }
                )
            }
            composable(Screen.AddSchedule.route) {
                AddScheduleScreen(onBackClick = { navController.popBackStack() }, onScheduleAdded = { navController.popBackStack() })
            }
            composable(route = Screen.EditSchedule.route, arguments = listOf(navArgument("scheduleId") { type = NavType.StringType })) {
                EditScheduleScreen(onBackClick = { navController.popBackStack() }, onScheduleAdded = { navController.popBackStack() })
            }
            composable(Screen.Grades.route) {
                GradesScreen(
                    onEditProfile = { navController.navigate(Screen.EditProfile.route) },
                    onSearchClick = { showGlobalSearchSheet = true }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    onEditProfile = { navController.navigate(Screen.EditProfile.route) },
                    onLogout = { navController.navigate(Screen.Onboarding.route) { popUpTo(0) { inclusive = true } } }
                )
            }
            composable(Screen.EditProfile.route) {
                EditProfileScreen(
                    onBack = { navController.popBackStack() },
                    onLogout = { navController.navigate(Screen.Onboarding.route) { popUpTo(0) { inclusive = true } } }
                )
            }
            composable(route = Screen.AddEvaluation.route, arguments = listOf(navArgument("subject") { type = NavType.StringType; defaultValue = "" })) { entry ->
                AddEvaluationScreen(subject = Uri.decode(entry.arguments?.getString("subject") ?: ""), onEvaluationAdded = { navController.popBackStack() }, onBack = { navController.popBackStack() })
            }
            composable(route = Screen.EditEvaluation.route, arguments = listOf(navArgument("evalId") { type = NavType.StringType })) {
                EditEvaluationScreen(onEvaluationUpdated = { navController.popBackStack() }, onBack = { navController.popBackStack() })
            }
        }

        if (showGlobalSearchSheet) {
            GlobalSearchBottomSheet(
                onDismiss = { showGlobalSearchSheet = false },
                onNavigateToRoute = { route -> showGlobalSearchSheet = false; navController.navigate(route) }
            )
        }
        activeNoteEditorArgs?.let { (noteId, subject) ->
            key(noteId ?: "new", subject ?: "") {
                NoteEditorBottomSheet(
                    noteId = noteId,
                    subject = subject,
                    onDismiss = { activeNoteEditorArgs = null },
                    onSaved = { activeNoteEditorArgs = null },
                    onCreateSubject = { showAddSubjectSheetFromEditor = true }
                )
            }
        }
        activeScheduleEditorId?.let { scheduleIdArg ->
            val targetId = if (scheduleIdArg == "NEW") null else scheduleIdArg
            key(scheduleIdArg) {
                ScheduleEditorBottomSheet(
                    scheduleId = targetId,
                    onDismiss = { activeScheduleEditorId = null },
                    onSaved = { activeScheduleEditorId = null },
                    onCreateSubject = { showAddSubjectSheetFromEditor = true }
                )
            }
        }
        activeEvaluationEditorArgs?.let { (evalId, subject) ->
            key(evalId ?: "new", subject ?: "") {
                EvaluationEditorBottomSheet(
                    evalId = evalId,
                    initialSubject = subject ?: "",
                    onDismiss = { activeEvaluationEditorArgs = null },
                    onSaved = { activeEvaluationEditorArgs = null },
                    onCreateSubject = { showAddSubjectSheetFromEditor = true }
                )
            }
        }
        if (showAddSubjectSheetFromEditor) {
            AddSubjectBottomSheet(
                onDismissRequest = { showAddSubjectSheetFromEditor = false },
                onSubjectCreated = { createdSubject ->
                    showAddSubjectSheetFromEditor = false
                    pendingSubjectTarget = createdSubject
                    if (activeEvaluationEditorArgs != null && activeEvaluationEditorArgs?.first == null) {
                        activeEvaluationEditorArgs = Pair(null, createdSubject)
                    }
                    if (activeNoteEditorArgs != null && activeNoteEditorArgs?.first == null) {
                        activeNoteEditorArgs = Pair(null, createdSubject)
                    }
                }
            )
        }
    }
}
