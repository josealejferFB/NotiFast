package com.notifast.app.presentation.feature.subjectdetail

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Note
import com.notifast.app.data.model.Schedule
import com.notifast.app.data.model.Teacher
import com.notifast.app.presentation.atoms.NotiFastStudyBackground
import com.notifast.app.presentation.feature.subjectdetail.components.SubjectDetailDialogs
import com.notifast.app.presentation.feature.subjectdetail.components.SubjectDetailHeroBento
import com.notifast.app.presentation.feature.subjectdetail.components.SubjectEvaluationsTab
import com.notifast.app.presentation.feature.subjectdetail.components.SubjectNoteDetailOverlay
import com.notifast.app.presentation.feature.subjectdetail.components.SubjectNotesTab
import com.notifast.app.presentation.feature.subjectdetail.components.SubjectPanoramaTab
import com.notifast.app.presentation.feature.subjectdetail.components.SubjectSchedulesTab
import com.notifast.app.presentation.feature.subjectdetail.components.SubjectSegmentedTabs

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
    var gradeDialogEval by remember { mutableStateOf<Evaluation?>(null) }
    var deleteEval by remember { mutableStateOf<Evaluation?>(null) }
    var deleteSchedule by remember { mutableStateOf<Schedule?>(null) }
    var showShareSheet by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedTeacher by remember { mutableStateOf<Teacher?>(null) }
    var expandedNote by remember { mutableStateOf<Note?>(null) }

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
                    onShareClick = { showShareSheet = true },
                    onEditClick = { showEditDialog = true },
                    subjectSection = uiState.subjectSection
                )
            }
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                uiState.error?.let { err ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        com.notifast.app.presentation.atoms.NotiFastAlertBanner(
                            message = err,
                            onDismiss = { viewModel.onEvent(SubjectDetailEvent.ClearError) }
                        )
                    }
                }
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
                                    modality = uiState.modality,
                                    teachers = uiState.teachers,
                                    studyDocuments = uiState.studyDocuments,
                                    onTeacherClick = { selectedTeacher = it },
                                    onWhatsAppClick = { selectedTeacher = it }
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
                                    subjectColorHex = uiState.subjectColorHex,
                                    onAddNoteClick = { onAddNoteClick(uiState.subjectName) },
                                    onNoteClick = { note -> expandedNote = note },
                                    onClearFilters = {
                                        viewModel.onEvent(SubjectDetailEvent.UpdateSearchQuery(""))
                                        viewModel.onEvent(SubjectDetailEvent.SelectTag(null))
                                    }
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

        val currentExpandedNote = expandedNote?.let { exp ->
            uiState.notes.find { it.id == exp.id } ?: exp
        }

        SubjectNoteDetailOverlay(
            note = currentExpandedNote,
            onDismiss = { expandedNote = null },
            onEditNote = { note ->
                onNoteClick(note)
                expandedNote = null
            },
            onTaskCheckedChange = { taskIndex, isChecked ->
                currentExpandedNote?.let { note ->
                    viewModel.onEvent(SubjectDetailEvent.ToggleTaskCheck(note, taskIndex, isChecked))
                }
            }
        )

        SubjectDetailDialogs(
            gradeDialogEval = gradeDialogEval,
            onDismissGradeDialog = { gradeDialogEval = null },
            onSaveGrade = { grade, isPresented ->
                gradeDialogEval?.let { eval ->
                    viewModel.onEvent(SubjectDetailEvent.SaveGrade(eval.id, grade, isPresented))
                }
            },
            gradeScale = uiState.gradeScale,
            deleteEval = deleteEval,
            onDismissDeleteEval = { deleteEval = null },
            onConfirmDeleteEval = { evalId ->
                viewModel.onEvent(SubjectDetailEvent.DeleteEvaluation(evalId))
            },
            deleteSchedule = deleteSchedule,
            onDismissDeleteSchedule = { deleteSchedule = null },
            onConfirmDeleteSchedule = { schedule ->
                viewModel.onEvent(SubjectDetailEvent.DeleteSchedule(schedule))
            },
            showShareSheet = showShareSheet,
            onDismissShareSheet = { showShareSheet = false },
            userId = uiState.userId,
            subjectName = uiState.subjectName,
            subjectColorHex = uiState.subjectColorHex,
            subjectIcon = uiState.subjectIcon,
            exportUseCase = viewModel.exportSubjectPackageUseCase,
            showEditDialog = showEditDialog,
            onDismissEditDialog = { showEditDialog = false },
            isRenaming = uiState.isRenaming,
            onSaveSubjectData = { newName, newColor, newIcon, newSection ->
                viewModel.onEvent(SubjectDetailEvent.UpdateSubjectData(newName, newColor, newIcon, newSection))
                showEditDialog = false
            },
            subjectSection = uiState.subjectSection,
            selectedTeacher = selectedTeacher,
            onDismissTeacherDetail = { selectedTeacher = null }
        )
    }
}
