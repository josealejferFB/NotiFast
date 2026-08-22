package com.notifast.app.presentation.feature.subjectdetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.notifast.app.data.model.Note
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.presentation.atoms.NotiFastCircularLoading
import com.notifast.app.presentation.atoms.NotiFastStudyBackground
import com.notifast.app.presentation.atoms.NotiFastExpandableFab
import com.notifast.app.presentation.atoms.FabAction
import com.notifast.app.presentation.feature.subjectdetail.components.SubjectDetailCalendarRow
import com.notifast.app.presentation.feature.subjectdetail.components.SubjectDetailHero
import com.notifast.app.presentation.feature.subjectdetail.components.SubjectDetailNotesMosaic
import com.notifast.app.presentation.feature.subjectdetail.components.SubjectDetailProgress
import com.notifast.app.presentation.feature.subjectdetail.components.SubjectDetailStats
import com.notifast.app.presentation.feature.subjectdetail.components.SubjectDetailTimeline
import com.notifast.app.presentation.theme.color.Aqua
import com.notifast.app.presentation.theme.color.Ruby
import com.notifast.app.domain.util.GradeConstants
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import com.notifast.app.data.model.Evaluation
import com.notifast.app.presentation.feature.subjectdetail.components.SubjectDetailEvaluationList

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
                            androidx.compose.material3.Surface(
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
            com.notifast.app.presentation.atoms.NotiFastDialog(
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

@Composable
private fun GradeDialog(
    evaluation: Evaluation,
    onSave: (Double?, Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var gradeText by remember { mutableStateOf(evaluation.grade?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "") }
    var isPresented by remember { mutableStateOf(evaluation.isPresented) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Calificar evaluación") },
        text = {
            Column {
                Text(evaluation.title, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = gradeText,
                    onValueChange = {
                        gradeText = it
                        error = null
                        val parsed = it.toDoubleOrNull()
                        if (it.isNotEmpty() && parsed != null && parsed in GradeConstants.MIN_GRADE..GradeConstants.MAX_GRADE) {
                            isPresented = true
                        }
                    },
                    label = { Text("Nota (${GradeConstants.MIN_GRADE} - ${GradeConstants.MAX_GRADE})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    isError = error != null,
                    supportingText = error?.let { { Text(it, color = Ruby) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isPresented = !isPresented }
                        .padding(vertical = 8.dp)
                ) {
                    androidx.compose.material3.Checkbox(
                        checked = isPresented,
                        onCheckedChange = { isPresented = it },
                        colors = androidx.compose.material3.CheckboxDefaults.colors(checkedColor = Aqua)
                    )
                    Text("Marcar como presentada", fontWeight = FontWeight.Medium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (gradeText.isBlank()) {
                        onSave(null, false)
                    } else {
                        val grade = gradeText.toDoubleOrNull()
                        when {
                            grade == null -> error = "Nota inválida"
                            grade < GradeConstants.MIN_GRADE || grade > GradeConstants.MAX_GRADE -> error = "La nota debe estar entre ${GradeConstants.MIN_GRADE} y ${GradeConstants.MAX_GRADE}"
                            else -> onSave(grade, isPresented)
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Ruby)
            ) { Text("Guardar", color = Color.White) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun SubjectDetailNoteDetail(
    note: Note,
    subjectColorHex: String,
    onClose: () -> Unit
) {
    val accentColor = try {
        if (subjectColorHex.isNotBlank()) Color(android.graphics.Color.parseColor(subjectColorHex))
        else Aqua
    } catch (e: Exception) { Aqua }

    Card(
        modifier = Modifier.fillMaxWidth().heightIn(max = 600.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            if (note.imageUrl != null) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    SubcomposeAsyncImage(model = note.imageUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp), contentScale = ContentScale.Crop)
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.align(Alignment.TopEnd).padding(12.dp).size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(24.dp))
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxWidth().height(56.dp).background(Brush.horizontalGradient(listOf(accentColor, accentColor.copy(alpha = 0.5f)))),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    IconButton(onClick = onClose, modifier = Modifier.padding(end = 8.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.White, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                Text(note.subject.uppercase(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = accentColor)
                Spacer(Modifier.height(4.dp))
                Text(note.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(16.dp))
                Text(note.content, style = MaterialTheme.typography.bodyMedium, lineHeight = 24.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = onClose,
                    modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f), contentColor = MaterialTheme.colorScheme.onSurface)
                ) {
                    Text("Cerrar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
