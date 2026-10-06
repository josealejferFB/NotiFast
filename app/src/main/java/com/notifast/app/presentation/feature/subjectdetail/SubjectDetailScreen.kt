package com.notifast.app.presentation.feature.subjectdetail

import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.R
import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Note
import com.notifast.app.data.model.Schedule
import com.notifast.app.presentation.atoms.NotiFastDialog
import com.notifast.app.presentation.atoms.NotiFastStudyBackground
import com.notifast.app.presentation.feature.subjectdetail.components.*

@Composable
fun SubjectDetailScreen(
    viewModel: SubjectDetailViewModel = hiltViewModel(),
    onBackClick: () -> Unit = {},
    onAddNoteClick: (String) -> Unit = {},
    onAddEvaluationClick: (String) -> Unit = {},
    onAddScheduleClick: (String) -> Unit = {},
    onNoteClick: (Note) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    var gradeDialogEval by remember { mutableStateOf<Evaluation?>(null) }
    var deleteEval by remember { mutableStateOf<Evaluation?>(null) }
    var deleteSchedule by remember { mutableStateOf<Schedule?>(null) }
    var showShareSheet by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            viewModel.onEvent(SubjectDetailEvent.ClearError)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastStudyBackground()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                SubjectDetailHeroBento(
                    subjectName = uiState.subjectName,
                    subjectIcon = uiState.subjectIcon,
                    subjectColorHex = uiState.subjectColorHex,
                    averageGrade = uiState.averageGrade,
                    displayScale = uiState.gradeScale,
                    evaluatedPercent = uiState.progressPercent,
                    onBackClick = onBackClick,
                    onAddNoteClick = { onAddNoteClick(uiState.subjectName) },
                    onAddEvaluationClick = { onAddEvaluationClick(uiState.subjectName) },
                    onAddScheduleClick = { onAddScheduleClick(uiState.subjectName) },
                    onShareClick = { showShareSheet = true }
                )
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                SubjectSegmentedTabs(
                    selectedQuadrant = uiState.selectedQuadrant,
                    onQuadrantSelected = { viewModel.onEvent(SubjectDetailEvent.SelectQuadrant(it)) }
                )

                LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = 64.dp)) {
                    item {
                        AnimatedContent(
                            targetState = uiState.selectedQuadrant,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "QuadrantTransition"
                        ) { quadrant ->
                            when (quadrant) {
                                SubjectQuadrant.PANORAMA -> SubjectPanoramaTab(
                                    milestone = uiState.nextMilestone,
                                    progressPercent = uiState.progressPercent,
                                    completedEvals = uiState.completedEvalCount,
                                    totalEvals = uiState.evaluations.size,
                                    simulationResult = uiState.simulationResult,
                                    scale = uiState.gradeScale,
                                    targetGrade = uiState.targetGrade,
                                    onTargetGradeChange = { viewModel.onEvent(SubjectDetailEvent.UpdateTargetGrade(it)) },
                                    classroom = uiState.classroom,
                                    teacher = uiState.teacher,
                                    modality = uiState.modality
                                )
                                SubjectQuadrant.EVALUATIONS -> SubjectEvaluationsTab(
                                    evaluations = uiState.evaluations,
                                    assignedWeight = uiState.totalAssignedWeight,
                                    gradedWeight = uiState.evaluatedWeight,
                                    availableWeight = uiState.availableWeight,
                                    displayScale = uiState.gradeScale,
                                    onAddEvaluationClick = { onAddEvaluationClick(uiState.subjectName) },
                                    onEditGradeClick = { gradeDialogEval = it },
                                    onDeleteEvaluationClick = { deleteEval = it }
                                )
                                SubjectQuadrant.NOTES -> SubjectNotesTab(
                                    notes = uiState.filteredNotes,
                                    searchQuery = uiState.searchQuery,
                                    onSearchQueryChange = { viewModel.onEvent(SubjectDetailEvent.UpdateSearchQuery(it)) },
                                    availableTags = uiState.availableTags,
                                    selectedTag = uiState.selectedTag,
                                    onTagSelect = { viewModel.onEvent(SubjectDetailEvent.SelectTag(it)) },
                                    subjectName = uiState.subjectName,
                                    onAddNoteClick = { onAddNoteClick(uiState.subjectName) },
                                    onNoteClick = onNoteClick
                                )
                                SubjectQuadrant.SCHEDULES -> SubjectSchedulesTab(
                                    schedules = uiState.schedules,
                                    onAddScheduleClick = { onAddScheduleClick(uiState.subjectName) },
                                    onDeleteScheduleClick = { deleteSchedule = it }
                                )
                            }
                        }
                    }
                }
            }
        }

        gradeDialogEval?.let { eval ->
            SubjectGradeQuickDialog(
                evaluation = eval,
                scale = uiState.gradeScale,
                onSave = { grade, isPresented ->
                    viewModel.onEvent(SubjectDetailEvent.SaveGrade(eval.id, grade, isPresented))
                    gradeDialogEval = null
                },
                onDismiss = { gradeDialogEval = null }
            )
        }

        deleteEval?.let { eval ->
            NotiFastDialog(
                title = stringResource(R.string.subject_detail_delete_eval_title),
                message = stringResource(R.string.subject_detail_delete_eval_msg, eval.title),
                confirmText = stringResource(R.string.btn_delete),
                dismissText = stringResource(R.string.btn_cancel),
                confirmColor = MaterialTheme.colorScheme.error,
                onConfirm = {
                    viewModel.onEvent(SubjectDetailEvent.DeleteEvaluation(eval.id))
                    deleteEval = null
                },
                onDismiss = { deleteEval = null }
            )
        }

        deleteSchedule?.let { schedule ->
            NotiFastDialog(
                title = stringResource(R.string.subject_detail_delete_schedule_title),
                message = stringResource(R.string.subject_detail_delete_schedule_msg, Schedule.DAY_NAMES.getOrElse(schedule.dayOfWeek) { "" }, schedule.startTime, schedule.endTime),
                confirmText = stringResource(R.string.btn_delete),
                dismissText = stringResource(R.string.btn_cancel),
                confirmColor = MaterialTheme.colorScheme.error,
                onConfirm = {
                    viewModel.onEvent(SubjectDetailEvent.DeleteSchedule(schedule))
                    deleteSchedule = null
                },
                onDismiss = { deleteSchedule = null }
            )
        }

        if (showShareSheet) {
            com.notifast.app.presentation.feature.share.ShareSubjectBottomSheet(
                userId = uiState.userId,
                subjectName = uiState.subjectName,
                subjectColorHex = uiState.subjectColorHex,
                exportUseCase = viewModel.exportSubjectPackageUseCase,
                onDismiss = { showShareSheet = false }
            )
        }
    }
}
