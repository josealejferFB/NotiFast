package com.notifast.app.presentation.feature.subjectdetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Note
import com.notifast.app.presentation.atoms.FabAction
import com.notifast.app.presentation.atoms.NotiFastCircularLoading
import com.notifast.app.presentation.atoms.NotiFastDialog
import com.notifast.app.presentation.atoms.NotiFastExpandableFab
import com.notifast.app.presentation.feature.grades.components.GradeSimulatorCard
import com.notifast.app.presentation.feature.subjectdetail.components.GradeDialog
import com.notifast.app.presentation.feature.subjectdetail.components.SubjectDetailEvaluationList
import com.notifast.app.presentation.feature.subjectdetail.components.SubjectDetailHero
import com.notifast.app.presentation.feature.subjectdetail.components.SubjectDetailNoteDetail
import com.notifast.app.presentation.feature.subjectdetail.components.SubjectDetailNotesMosaic
import com.notifast.app.presentation.feature.subjectdetail.components.SubjectDetailProgress
import com.notifast.app.presentation.feature.subjectdetail.components.SubjectDetailStats
import com.notifast.app.presentation.feature.subjectdetail.components.SubjectDetailTimeline
import com.notifast.app.presentation.theme.color.Aqua
import com.notifast.app.presentation.theme.color.Ruby

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(
    viewModel: SubjectDetailViewModel = hiltViewModel(),
    onAddNoteClick: (String) -> Unit = {},
    onAddEvaluationClick: (String) -> Unit = {},
    onAddScheduleClick: (String) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var expandedNote by remember { mutableStateOf<Note?>(null) }
    var gradeDialogEval by remember { mutableStateOf<Evaluation?>(null) }
    var deleteEvalId by remember { mutableStateOf<String?>(null) }
    val isHeaderVisible = expandedNote == null
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
        if (uiState.isLoading) { NotiFastCircularLoading(); return@Box }

        AnimatedVisibility(
            visible = isHeaderVisible,
            enter = fadeIn(tween(400)),
            exit = fadeOut(tween(200))
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                floatingActionButton = {
                    NotiFastExpandableFab(
                        actions = listOf(
                            FabAction("Nuevo Horario", Icons.Default.Schedule, { onAddScheduleClick(uiState.subjectName) }, "Añadir Horario", Aqua),
                            FabAction("Nueva Evaluación", Icons.Default.CalendarMonth, { onAddEvaluationClick(uiState.subjectName) }, "Añadir Evaluación", Aqua),
                            FabAction("Nuevo Apunte", Icons.Default.Edit, { onAddNoteClick(uiState.subjectName) }, "Añadir Apunte", Aqua)
                        )
                    )
                }
            ) { padding ->
                Column(modifier = Modifier.fillMaxSize().padding(bottom = padding.calculateBottomPadding())) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 140.dp)
                    ) {
                        item(key = "hero") {
                            SubjectDetailHero(
                                subjectName = uiState.subjectName,
                                subjectIcon = uiState.subjectIcon,
                                subjectColorHex = uiState.subjectColorHex,
                                noteCount = uiState.notes.size,
                                evalCount = uiState.evaluations.size,
                                onBackClick = onBackClick
                            )
                        }

                        @OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
                        stickyHeader(key = "tabs") {
                            Surface(
                                color = MaterialTheme.colorScheme.background,
                                shadowElevation = 2.dp
                            ) {
                                TabRow(
                                    selectedTabIndex = selectedTabIndex,
                                    containerColor = Color.Transparent,
                                    contentColor = Ruby,
                                    indicator = { tabPositions ->
                                        TabRowDefaults.SecondaryIndicator(
                                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                            color = Ruby
                                        )
                                    }
                                ) {
                                    Tab(
                                        selected = selectedTabIndex == 0,
                                        onClick = { selectedTabIndex = 0 },
                                        text = { Text("Resumen", fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Medium) }
                                    )
                                    Tab(
                                        selected = selectedTabIndex == 1,
                                        onClick = { selectedTabIndex = 1 },
                                        text = { Text("Evaluaciones", fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Medium) }
                                    )
                                    Tab(
                                        selected = selectedTabIndex == 2,
                                        onClick = { selectedTabIndex = 2 },
                                        text = { Text("Apuntes", fontWeight = if (selectedTabIndex == 2) FontWeight.Bold else FontWeight.Medium) }
                                    )
                                }
                            }
                        }

                        when (selectedTabIndex) {
                            0 -> {
                                item(key = "stats") {
                                    SubjectDetailStats(
                                        averageGrade = uiState.averageGrade,
                                        completedEvalCount = uiState.completedEvalCount,
                                        pendingEvalCount = uiState.pendingEvalCount,
                                        totalEvalCount = uiState.evaluations.size,
                                        noteCount = uiState.notes.size,
                                        subjectColorHex = uiState.subjectColorHex
                                    )
                                }
                                item(key = "progress") {
                                    SubjectDetailProgress(
                                        progressPercent = uiState.progressPercent,
                                        completedCount = uiState.completedEvalCount,
                                        totalCount = uiState.evaluations.size,
                                        subjectColorHex = uiState.subjectColorHex
                                    )
                                }
                                item(key = "simulator") {
                                    Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp)) {
                                        GradeSimulatorCard(
                                            simulationResult = uiState.simulationResult,
                                            scale = uiState.gradeScale,
                                            targetGrade = uiState.targetGrade,
                                            onTargetGradeChange = { viewModel.onEvent(SubjectDetailEvent.UpdateTargetGrade(it)) }
                                        )
                                    }
                                }
                            }
                            1 -> {
                                item(key = "timeline") {
                                    SubjectDetailTimeline(
                                        upcomingEvaluations = uiState.upcomingEvaluations,
                                        subjectColorHex = uiState.subjectColorHex,
                                        onMarkComplete = { gradeDialogEval = it },
                                        onDeleteEval = { deleteEvalId = it.id }
                                    )
                                    Spacer(Modifier.height(24.dp))
                                }
                                item(key = "all-evaluations") {
                                    SubjectDetailEvaluationList(
                                        evaluations = uiState.evaluations,
                                        subjectColorHex = uiState.subjectColorHex,
                                        onMarkComplete = { gradeDialogEval = it },
                                        onEditGrade = { gradeDialogEval = it },
                                        onDeleteEval = { deleteEvalId = it.id }
                                    )
                                    Spacer(Modifier.height(24.dp))
                                }
                            }
                            2 -> {
                                item(key = "notes") {
                                    SubjectDetailNotesMosaic(
                                        notes = uiState.notes,
                                        subjectColorHex = uiState.subjectColorHex,
                                        onNoteClick = { expandedNote = it }
                                    )
                                    Spacer(Modifier.height(24.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = !isHeaderVisible,
            enter = fadeIn(tween(400)) + scaleIn(initialScale = 0.85f, animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f)),
            exit = fadeOut(tween(250)) + scaleOut(targetScale = 0.85f, animationSpec = spring(stiffness = 300f))
        ) {
            Box(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp, vertical = 24.dp), contentAlignment = Alignment.Center) {
                expandedNote?.let { note ->
                    SubjectDetailNoteDetail(
                        note = note,
                        subjectColorHex = uiState.subjectColorHex,
                        onClose = { expandedNote = null }
                    )
                }
            }
        }

        gradeDialogEval?.let { eval ->
            GradeDialog(
                evaluation = eval,
                onSave = { grade, isPresented ->
                    viewModel.onEvent(SubjectDetailEvent.SaveGrade(eval.id, grade, isPresented = isPresented))
                    gradeDialogEval = null
                },
                onDismiss = { gradeDialogEval = null }
            )
        }

        deleteEvalId?.let { id ->
            val eval = uiState.evaluations.find { it.id == id }
            NotiFastDialog(
                title = "¿Eliminar evaluación?",
                message = "¿Estás seguro de eliminar ${eval?.title ?: ""}? Esta acción no se puede deshacer.",
                confirmText = "Eliminar",
                dismissText = "Cancelar",
                confirmColor = Ruby,
                confirmWeight = FontWeight.Bold,
                onConfirm = {
                    viewModel.onEvent(SubjectDetailEvent.DeleteEvaluation(id))
                    deleteEvalId = null
                },
                onDismiss = { deleteEvalId = null }
            )
        }
    }
}
