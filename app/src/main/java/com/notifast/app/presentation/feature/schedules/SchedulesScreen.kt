package com.notifast.app.presentation.feature.schedules

import android.content.Intent
import android.net.Uri
import com.notifast.app.presentation.atoms.AlertSeverity
import com.notifast.app.presentation.atoms.rememberNotiFastAlert
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import com.notifast.app.R
import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Schedule
import com.notifast.app.presentation.atoms.NotiFastCardSkeleton
import com.notifast.app.presentation.atoms.NotiFastExpandableFab
import com.notifast.app.presentation.atoms.NotiFastStudyBackground
import com.notifast.app.presentation.feature.schedules.components.AgendaMonthCalendarView
import com.notifast.app.presentation.feature.schedules.components.ScheduleDaySelector
import com.notifast.app.presentation.feature.schedules.components.ScheduleLiveStatusCard
import com.notifast.app.presentation.feature.schedules.components.ScheduleTimelineView
import com.notifast.app.presentation.feature.schedules.components.ScheduleWeekBentoSummary
import com.notifast.app.presentation.feature.schedules.components.SchedulesDialogsHost

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchedulesScreen(
    viewModel: SchedulesViewModel = hiltViewModel(),
    onAddScheduleClick: () -> Unit = {},
    onAddEvaluationClick: () -> Unit = {},
    onTakeNoteClick: (String) -> Unit = {},
    onEditScheduleClick: (String) -> Unit = {},
    onEditEvaluationClick: (Evaluation) -> Unit = {},
    onEditProfile: () -> Unit = {},
    onSearchClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var scheduleToDelete by remember { mutableStateOf<Schedule?>(null) }
    var evalToDelete by remember { mutableStateOf<Evaluation?>(null) }
    var selectedEvaluation by remember { mutableStateOf<Evaluation?>(null) }
    val context = LocalContext.current
    val showAlert = rememberNotiFastAlert()

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.onEvent(SchedulesEvent.ParseUnergPdf(it, context)) }
    }

    LaunchedEffect(uiState.importSuccessMessage) {
        uiState.importSuccessMessage?.let { msg ->
            showAlert(msg, AlertSeverity.SUCCESS)
            viewModel.onEvent(SchedulesEvent.ClearImportSuccess)
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.onEvent(SchedulesEvent.RefreshLiveStatus)
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            delay(30_000L)
            viewModel.onEvent(SchedulesEvent.RefreshLiveStatus)
        }
    }

    val openMeeting: (String) -> Unit = { url ->
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (e: Exception) {
            android.util.Log.w("SchedulesScreen", "Could not open meeting link: $url", e)
            showAlert(context.getString(R.string.error_open_link), AlertSeverity.ERROR)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastStudyBackground()
        Scaffold(
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            floatingActionButton = {
                NotiFastExpandableFab(
                    actions = buildSchedulesFabActions(
                        scheduleLabel = stringResource(R.string.agenda_create_schedule_option),
                        evaluationLabel = stringResource(R.string.agenda_create_evaluation_option),
                        scheduleColor = MaterialTheme.colorScheme.primary,
                        evaluationColor = MaterialTheme.colorScheme.error,
                        onAddScheduleClick = onAddScheduleClick,
                        onAddEvaluationClick = onAddEvaluationClick,
                        unergImportLabel = stringResource(R.string.unerg_import_button_title),
                        unergImportColor = MaterialTheme.colorScheme.tertiary,
                        onUnergImportClick = { pdfPickerLauncher.launch("application/pdf") }
                    ),
                    modifier = Modifier.padding(bottom = 72.dp)
                )
            }
        ) { innerPadding ->
            if (uiState.isLoading) {
                Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(horizontal = 16.dp)) {
                    Spacer(Modifier.height(16.dp))
                    repeat(3) { NotiFastCardSkeleton(); Spacer(Modifier.height(12.dp)) }
                }
                return@Scaffold
            }

            val classCounts = remember(uiState.groupedSchedules) { uiState.groupedSchedules.mapValues { it.value.size } }

            Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                uiState.error?.let { err ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        com.notifast.app.presentation.atoms.NotiFastAlertBanner(
                            message = err,
                            onDismiss = { viewModel.onEvent(SchedulesEvent.ClearError) }
                        )
                    }
                }

                if (uiState.schedules.isNotEmpty()) {
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        ScheduleLiveStatusCard(status = uiState.liveClassStatus, onTakeNote = onTakeNoteClick, onJoinMeeting = openMeeting)
                    }
                }

                ScheduleDaySelector(
                    selectedDay = uiState.selectedDay,
                    classCountsByDay = classCounts,
                    evaluationCountsByDay = uiState.evaluationCountsByDay,
                    viewMode = uiState.viewMode,
                    onDaySelected = { viewModel.onEvent(SchedulesEvent.SelectDay(it)) },
                    onViewModeChanged = { viewModel.onEvent(SchedulesEvent.SetViewMode(it)) },
                    firstDayOfWeek = uiState.firstDayOfWeek
                )

                AnimatedContent(
                    targetState = uiState.viewMode,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "AgendaViewTransition",
                    modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp)
                ) { targetMode ->
                    when (targetMode) {
                        ScheduleViewMode.DAILY_TIMELINE -> {
                            ScheduleTimelineView(
                                items = uiState.timelineItems,
                                conflicts = uiState.conflicts,
                                dailyEvaluations = uiState.dailyEvaluations,
                                onEvaluationClick = { selectedEvaluation = it },
                                onTakeNote = onTakeNoteClick,
                                onEditSchedule = onEditScheduleClick,
                                onDeleteSchedule = { scheduleToDelete = it },
                                onJoinMeeting = openMeeting,
                                displayScale = uiState.gradeScale
                            )
                        }
                        ScheduleViewMode.MONTHLY_CALENDAR -> {
                            AgendaMonthCalendarView(
                                currentMonth = uiState.currentMonth,
                                selectedDate = uiState.selectedDate,
                                evaluationsByDate = uiState.evaluationsByDate,
                                dailyEvaluations = uiState.dailyEvaluations,
                                onDateSelected = { viewModel.onEvent(SchedulesEvent.SelectDate(it)) },
                                onChangeMonth = { viewModel.onEvent(SchedulesEvent.ChangeMonth(it)) },
                                onGoToToday = { viewModel.onEvent(SchedulesEvent.GoToToday) },
                                onEvaluationClick = { selectedEvaluation = it },
                                firstDayOfWeek = uiState.firstDayOfWeek,
                                displayScale = uiState.gradeScale
                            )
                        }
                        ScheduleViewMode.WEEKLY_BENTO -> {
                            ScheduleWeekBentoSummary(summary = uiState.weeklySummary)
                        }
                    }
                }
            }
        }

        SchedulesDialogsHost(
            uiState = uiState,
            selectedEvaluation = selectedEvaluation,
            scheduleToDelete = scheduleToDelete,
            evalToDelete = evalToDelete,
            onDismissEvaluationSheet = { selectedEvaluation = null },
            onEditEvaluationClick = onEditEvaluationClick,
            onDeleteEvaluationRequest = { evalToDelete = it },
            onDismissDeleteSchedule = { scheduleToDelete = null },
            onDismissDeleteEvaluation = { evalToDelete = null },
            onEvent = viewModel::onEvent
        )
    }
}
