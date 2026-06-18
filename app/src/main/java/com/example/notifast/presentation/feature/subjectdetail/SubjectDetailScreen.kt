package com.example.notifast.presentation.feature.subjectdetail

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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import com.example.notifast.data.model.Note
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notifast.presentation.atoms.NotiFastCircularLoading
import com.example.notifast.presentation.atoms.NotiFastStudyBackground
import com.example.notifast.presentation.feature.subjectdetail.components.SubjectDetailCalendarRow
import com.example.notifast.presentation.feature.subjectdetail.components.SubjectDetailHero
import com.example.notifast.presentation.feature.subjectdetail.components.SubjectDetailNotesMosaic
import com.example.notifast.presentation.feature.subjectdetail.components.SubjectDetailProgress
import com.example.notifast.presentation.feature.subjectdetail.components.SubjectDetailStats
import com.example.notifast.presentation.feature.subjectdetail.components.SubjectDetailTimeline
import com.example.notifast.presentation.theme.color.Aqua
import com.example.notifast.presentation.theme.color.Ruby
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.input.KeyboardType
import com.example.notifast.data.model.Evaluation
import com.example.notifast.presentation.feature.subjectdetail.components.SubjectDetailEvaluationList

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubjectDetailScreen(
    viewModel: SubjectDetailViewModel = hiltViewModel(),
    onAddNoteClick: (String) -> Unit = {},
    onAddEvaluationClick: (String) -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var expandedNote by remember { mutableStateOf<Note?>(null) }
    var gradeDialogEval by remember { mutableStateOf<Evaluation?>(null) }
    var deleteEvalId by remember { mutableStateOf<String?>(null) }
    val isHeaderVisible = expandedNote == null

    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastStudyBackground()
        if (uiState.isLoading) { NotiFastCircularLoading(); return@Box }

        AnimatedVisibility(
            visible = isHeaderVisible,
            enter = fadeIn(tween(400)),
            exit = fadeOut(tween(200))
        ) {
            Scaffold(
                containerColor = Color.Transparent,
                topBar = {
                    Box(modifier = Modifier.fillMaxWidth().statusBarsPadding()) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                        }
                    }
                },
                floatingActionButton = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        FloatingActionButton(
                            onClick = { onAddNoteClick(uiState.subjectName) },
                            containerColor = Ruby,
                            contentColor = Color.White,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Agregar apunte", modifier = Modifier.size(24.dp))
                        }
                        FloatingActionButton(
                            onClick = { onAddEvaluationClick(uiState.subjectName) },
                            containerColor = Aqua,
                            contentColor = Color.White,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "Agregar evaluación", modifier = Modifier.size(24.dp))
                        }
                    }
                }
            ) { padding ->
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

                    item(key = "timeline") {
                        SubjectDetailTimeline(
                            upcomingEvaluations = uiState.upcomingEvaluations,
                            subjectColorHex = uiState.subjectColorHex,
                            onMarkComplete = { gradeDialogEval = it },
                            onDeleteEval = { deleteEvalId = it.id }
                        )
                        Spacer(Modifier.height(24.dp))
                    }

                    item(key = "calendar") {
                        SubjectDetailCalendarRow(
                            evaluations = uiState.evaluations.filter { it.date != null },
                            subjectColorHex = uiState.subjectColorHex
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

                    item(key = "notes") {
                        SubjectDetailNotesMosaic(
                            notes = uiState.notes,
                            subjectColorHex = uiState.subjectColorHex,
                            onNoteClick = { expandedNote = it }
                        )
                        Spacer(Modifier.height(24.dp))
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
                onSave = { grade ->
                    viewModel.onEvent(SubjectDetailEvent.SaveGrade(eval.id, grade, isPresented = true))
                    gradeDialogEval = null
                },
                onDismiss = { gradeDialogEval = null }
            )
        }

        deleteEvalId?.let { id ->
            val eval = uiState.evaluations.find { it.id == id }
            AlertDialog(
                onDismissRequest = { deleteEvalId = null },
                title = { Text("Eliminar evaluaci\u00F3n") },
                text = { Text("\u00BFEst\u00E1s seguro de eliminar ${eval?.title ?: ""}? Esta acci\u00F3n no se puede deshacer.") },
                confirmButton = {
                    Button(
                        onClick = {
                            viewModel.onEvent(SubjectDetailEvent.DeleteEvaluation(id))
                            deleteEvalId = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Ruby)
                    ) { Text("Eliminar", color = Color.White) }
                },
                dismissButton = {
                    TextButton(onClick = { deleteEvalId = null }) { Text("Cancelar") }
                }
            )
        }
    }
}

@Composable
private fun GradeDialog(
    evaluation: Evaluation,
    onSave: (Double) -> Unit,
    onDismiss: () -> Unit
) {
    var gradeText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Calificar evaluaci\u00F3n") },
        text = {
            Column {
                Text(evaluation.title, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = gradeText,
                    onValueChange = {
                        gradeText = it
                        error = null
                    },
                    label = { Text("Nota (0.0 - 10.0)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = error != null,
                    supportingText = error?.let { { Text(it, color = Ruby) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val grade = gradeText.toDoubleOrNull()
                    when {
                        gradeText.isBlank() -> error = "Ingresa una nota"
                        grade == null -> error = "Nota inv\u00E1lida"
                        grade < 0.0 || grade > 10.0 -> error = "La nota debe estar entre 0.0 y 10.0"
                        else -> onSave(grade)
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth().verticalScroll(rememberScrollState())) {
            if (note.imageUrl != null) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    SubcomposeAsyncImage(model = note.imageUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp), contentScale = ContentScale.Crop)
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.align(Alignment.TopEnd).padding(12.dp).size(32.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.9f))
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar", tint = Color.DarkGray, modifier = Modifier.size(18.dp))
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
                Text(note.content, style = MaterialTheme.typography.bodyMedium, lineHeight = 24.sp, color = Color.DarkGray)
                Spacer(Modifier.height(32.dp))
                androidx.compose.material3.Button(
                    onClick = onClose,
                    modifier = Modifier.align(Alignment.CenterHorizontally).fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color.LightGray.copy(alpha = 0.2f), contentColor = Color.DarkGray)
                ) {
                    Text("Cerrar", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
