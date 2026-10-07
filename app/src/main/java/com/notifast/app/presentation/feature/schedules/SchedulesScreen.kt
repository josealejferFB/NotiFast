package com.notifast.app.presentation.feature.schedules

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.R
import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Schedule
import com.notifast.app.presentation.atoms.NotiFastCardSkeleton
import com.notifast.app.presentation.atoms.NotiFastDialog
import com.notifast.app.presentation.atoms.NotiFastStudyBackground
import com.notifast.app.presentation.atoms.NotiFastTopBar
import com.notifast.app.presentation.atoms.TopBarType
import com.notifast.app.presentation.feature.schedules.components.AgendaCreateActionBottomSheet
import com.notifast.app.presentation.feature.schedules.components.AgendaEvaluationBottomSheet
import com.notifast.app.presentation.feature.schedules.components.AgendaMonthCalendarView
import com.notifast.app.presentation.feature.schedules.components.ScheduleDaySelector
import com.notifast.app.presentation.feature.schedules.components.ScheduleLiveStatusCard
import com.notifast.app.presentation.feature.schedules.components.ScheduleTimelineView
import com.notifast.app.presentation.feature.schedules.components.ScheduleWeekBentoSummary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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
    var showCreateSheet by remember { mutableStateOf(false) }

    val createSheetState = rememberModalBottomSheetState()
    val evalSheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            Toast.makeText(context, uiState.error, Toast.LENGTH_LONG).show()
            viewModel.onEvent(SchedulesEvent.ClearError)
        }
    }

    val openMeeting: (String) -> Unit = { url ->
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        } catch (_: Exception) {
            Toast.makeText(context, "No se pudo abrir el enlace", Toast.LENGTH_SHORT).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastStudyBackground()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                NotiFastTopBar(
                    type = TopBarType.Home(
                        firstName = uiState.userFirstName,
                        email = uiState.userEmail,
                        profilePictureUrl = uiState.userProfilePictureUrl,
                        contextText = stringResource(R.string.agenda_title),
                        onProfileClick = onEditProfile,
                        onSearchClick = onSearchClick
                    )
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showCreateSheet = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(0.dp),
                    modifier = Modifier.padding(bottom = 72.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.agenda_btn_add))
                }
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
            val evalCounts = remember(uiState.evaluations) {
                val cal = Calendar.getInstance()
                uiState.evaluations.filter { it.date != null }.groupBy {
                    cal.time = it.date!!
                    (cal.get(Calendar.DAY_OF_WEEK) - 1).let { d -> if (d == 0) 0 else d }
                }.mapValues { it.value.size }
            }

            Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                if (uiState.schedules.isNotEmpty()) {
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                        ScheduleLiveStatusCard(status = uiState.liveClassStatus, onTakeNote = onTakeNoteClick, onJoinMeeting = openMeeting)
                    }
                }

                ScheduleDaySelector(
                    selectedDay = uiState.selectedDay,
                    classCountsByDay = classCounts,
                    evaluationCountsByDay = evalCounts,
                    viewMode = uiState.viewMode,
                    onDaySelected = { viewModel.onEvent(SchedulesEvent.SelectDay(it)) },
                    onViewModeChanged = { viewModel.onEvent(SchedulesEvent.SetViewMode(it)) }
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

        if (showCreateSheet) {
            ModalBottomSheet(
                onDismissRequest = { showCreateSheet = false },
                sheetState = createSheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                AgendaCreateActionBottomSheet(
                    onAddSchedule = { scope.launch { createSheetState.hide() }.invokeOnCompletion { showCreateSheet = false; onAddScheduleClick() } },
                    onAddEvaluation = { scope.launch { createSheetState.hide() }.invokeOnCompletion { showCreateSheet = false; onAddEvaluationClick() } }
                )
            }
        }

        if (selectedEvaluation != null) {
            ModalBottomSheet(
                onDismissRequest = { selectedEvaluation = null },
                sheetState = evalSheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                selectedEvaluation?.let { eval ->
                    AgendaEvaluationBottomSheet(
                        eval = eval,
                        displayScale = uiState.gradeScale,
                        onEdit = { scope.launch { evalSheetState.hide() }.invokeOnCompletion { selectedEvaluation = null; onEditEvaluationClick(eval) } },
                        onDelete = { evalToDelete = eval; selectedEvaluation = null }
                    )
                }
            }
        }

        scheduleToDelete?.let { schedule ->
            NotiFastDialog(
                title = stringResource(R.string.schedules_delete_title),
                message = stringResource(R.string.schedules_delete_message, schedule.subjectName, schedule.startTime, schedule.endTime),
                confirmText = stringResource(R.string.btn_delete),
                dismissText = stringResource(R.string.btn_cancel),
                confirmColor = MaterialTheme.colorScheme.error,
                confirmWeight = FontWeight.Bold,
                onConfirm = { viewModel.onEvent(SchedulesEvent.DeleteSchedule(schedule)); scheduleToDelete = null },
                onDismiss = { scheduleToDelete = null }
            )
        }

        evalToDelete?.let { eval ->
            NotiFastDialog(
                title = stringResource(R.string.agenda_delete_eval_title),
                message = stringResource(R.string.agenda_delete_eval_message, eval.title),
                confirmText = stringResource(R.string.btn_delete),
                dismissText = stringResource(R.string.btn_cancel),
                confirmColor = MaterialTheme.colorScheme.error,
                confirmWeight = FontWeight.Bold,
                onConfirm = { viewModel.onEvent(SchedulesEvent.DeleteEvaluation(eval)); evalToDelete = null },
                onDismiss = { evalToDelete = null }
            )
        }
    }
}
