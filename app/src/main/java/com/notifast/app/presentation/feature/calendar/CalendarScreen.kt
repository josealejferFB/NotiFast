package com.notifast.app.presentation.feature.calendar

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.data.model.Evaluation
import com.notifast.app.presentation.atoms.NotiFastCircularLoading
import com.notifast.app.presentation.atoms.NotiFastDialog
import com.notifast.app.presentation.atoms.NotiFastEmptyState
import com.notifast.app.presentation.atoms.NotiFastStudyBackground
import com.notifast.app.presentation.atoms.NotiFastTopBar
import com.notifast.app.presentation.atoms.TopBarType
import com.notifast.app.presentation.feature.calendar.components.CalendarGrid
import com.notifast.app.presentation.feature.calendar.components.EvaluationDetailBottomSheet
import com.notifast.app.presentation.feature.calendar.components.EvaluationListItem
import com.notifast.app.presentation.theme.color.Ruby
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel(),
    onAddEvaluation: () -> Unit,
    onEditEvaluation: (Evaluation) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var selectedEvaluation by remember { mutableStateOf<Evaluation?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Evaluation?>(null) }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()

    val context = LocalContext.current
    LaunchedEffect(uiState.error) {
        if (uiState.error != null) {
            Toast.makeText(context, uiState.error, Toast.LENGTH_LONG).show()
            viewModel.onEvent(CalendarEvent.ClearError)
        }
    }

    if (showDeleteDialog != null) {
        NotiFastDialog(
            title = "¿Eliminar evaluación?",
            message = "Esta acción no se puede deshacer.",
            onConfirm = {
                showDeleteDialog?.let { viewModel.onEvent(CalendarEvent.DeleteEvaluation(it)) }
                showDeleteDialog = null
                selectedEvaluation = null
            },
            onDismiss = { showDeleteDialog = null },
            confirmText = "Eliminar",
            confirmColor = MaterialTheme.colorScheme.error,
            confirmWeight = FontWeight.Bold
        )
    }

    if (uiState.isLoading) {
        NotiFastCircularLoading()
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastStudyBackground()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                NotiFastTopBar(
                    type = TopBarType.Simple(
                        title = "Mi Calendario"
                    )
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = padding.calculateTopPadding(), start = 16.dp, end = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                // Header (Month Navigation)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { viewModel.onEvent(CalendarEvent.ChangeMonth(-1)) }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = null)
                    }

                    Text(
                        text = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
                            .format(uiState.currentMonth.time)
                            .replaceFirstChar { it.uppercase() },
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { viewModel.onEvent(CalendarEvent.GoToToday) }) {
                            Icon(Icons.Default.Today, contentDescription = "Ir a hoy", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { viewModel.onEvent(CalendarEvent.ChangeMonth(1)) }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        }
                    }
                }

                CalendarGrid(
                    currentMonth = uiState.currentMonth,
                    selectedDate = uiState.selectedDate,
                    onDateSelected = { viewModel.onEvent(CalendarEvent.SelectDate(it)) },
                    evaluationsByDate = uiState.evaluationsByDate
                )
                Spacer(modifier = Modifier.height(24.dp))

                val dailyEvals = uiState.dailyEvaluations
                val subjectsWithEval = dailyEvals.map { it.subjectName }.toSet()

                Text(
                    text = if (subjectsWithEval.isNotEmpty()) "¡Día de evaluación!" else "Eventos del día",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (subjectsWithEval.isNotEmpty()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Box(modifier = Modifier.weight(1f)) {
                    if (dailyEvals.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(bottom = 100.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            NotiFastEmptyState(
                                icon = Icons.Default.Event,
                                title = "Día libre",
                                message = "No tienes actividades programadas para este día."
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 130.dp)
                        ) {
                            items(dailyEvals) { eval ->
                                EvaluationListItem(
                                    eval = eval,
                                    onClick = { selectedEvaluation = eval }
                                )
                            }
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(end = 16.dp, bottom = 116.dp),
                contentAlignment = Alignment.BottomEnd
            ) {
                FloatingActionButton(
                    onClick = onAddEvaluation,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Nueva Evaluación")
                }
            }
        }
    }

    // Modal Bottom Sheet para detalles
    if (selectedEvaluation != null) {
        ModalBottomSheet(
            onDismissRequest = { selectedEvaluation = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = { androidx.compose.material3.BottomSheetDefaults.DragHandle() }
        ) {
            selectedEvaluation?.let { eval ->
                EvaluationDetailBottomSheet(
                    eval = eval,
                    onEdit = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            selectedEvaluation = null
                            onEditEvaluation(eval)
                        }
                    },
                    onDelete = { showDeleteDialog = eval }
                )
            }
        }
    }
}
