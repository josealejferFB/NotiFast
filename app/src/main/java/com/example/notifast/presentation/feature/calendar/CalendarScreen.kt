package com.example.notifast.presentation.feature.calendar

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Note
import androidx.compose.material.icons.automirrored.filled.Subject
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.notifast.data.model.Evaluation
import com.example.notifast.presentation.atoms.NotiFastCircularLoading
import com.example.notifast.presentation.atoms.NotiFastDialog
import com.example.notifast.presentation.atoms.NotiFastEmptyState
import com.example.notifast.presentation.atoms.NotiFastStudyBackground
import com.example.notifast.data.model.EvaluationType
import com.example.notifast.presentation.theme.color.Aqua
import com.example.notifast.presentation.theme.color.Ruby
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel = hiltViewModel(),
    onAddEvaluation: () -> Unit,
    onEditEvaluation: (Evaluation) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
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
            confirmColor = Ruby,
            confirmWeight = FontWeight.Bold
        )
    }

    if (uiState.isLoading) {
        NotiFastCircularLoading()
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastStudyBackground()
        Scaffold(containerColor = Color.Transparent, topBar = {
            com.example.notifast.presentation.atoms.NotiFastTopBar(
                type = com.example.notifast.presentation.atoms.TopBarType.Detail(
                    title = "Mi Calendario",
                    onBackClick = { }
                )
            )
        }) { padding ->
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
                    IconButton(onClick = {
                        val newMonth = currentMonth.clone() as Calendar
                        newMonth.add(Calendar.MONTH, -1)
                        currentMonth = newMonth
                    }) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = null)
                    }

                    Text(
                        text = SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(currentMonth.time).replaceFirstChar { it.uppercase() },
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Botón de "Hoy"
                        IconButton(onClick = {
                            val today = Calendar.getInstance()
                            currentMonth = today.clone() as Calendar
                            selectedDate = today.clone() as Calendar
                        }) {
                            Icon(Icons.Default.Today, contentDescription = "Ir a hoy", tint = Ruby)
                        }
                        IconButton(onClick = {
                            val newMonth = currentMonth.clone() as Calendar
                            newMonth.add(Calendar.MONTH, 1)
                            currentMonth = newMonth
                        }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        }
                    }
                }

                CalendarGrid(
                    currentMonth = currentMonth,
                    selectedDate = selectedDate,
                    onDateSelected = {
                        selectedDate = it
                    },
                    evaluations = uiState.evaluations
                )
                Spacer(modifier = Modifier.height(24.dp))

                val dailyEvals = uiState.evaluations.filter { isSameDay(it.date, selectedDate.time) }
                val subjectsWithEval = dailyEvals.map { it.subjectName }.toSet()

                Text(
                    text = if (subjectsWithEval.isNotEmpty()) "¡Día de evaluación!" else "Eventos del día",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (subjectsWithEval.isNotEmpty()) Ruby else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Box(modifier = Modifier.weight(1f)) {
                    if (dailyEvals.isEmpty()) {
                        Box(Modifier.fillMaxSize().padding(bottom = 100.dp), contentAlignment = Alignment.Center) {
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
                            contentPadding = PaddingValues(bottom = 130.dp) // Safe area para FAB y BottomNav
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

            Box(modifier = Modifier.fillMaxSize().padding(end = 16.dp, bottom = 116.dp), contentAlignment = Alignment.BottomEnd) {
                FloatingActionButton(
                    onClick = onAddEvaluation,
                    containerColor = Ruby,
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
                EvaluationDetailContent(
                    eval = eval,
                    onEdit = {
                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                            selectedEvaluation = null
                            onEditEvaluation(eval)
                        }
                    },
                    onDelete = {
                        showDeleteDialog = eval
                    }
                )
            }
        }
    }
}

@Composable
fun EvaluationListItem(eval: Evaluation, onClick: () -> Unit) {
    val evaluationType = EvaluationType.fromName(eval.type)
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = Modifier.fillMaxWidth().clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(evaluationType.color.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(evaluationType.icon, contentDescription = null, tint = evaluationType.color)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(eval.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(2.dp))
                Text(eval.subjectName, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun EvaluationDetailContent(eval: Evaluation, onEdit: () -> Unit, onDelete: () -> Unit) {
    val evaluationType = EvaluationType.fromName(eval.type)

    Column(modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp).padding(bottom = 32.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(56.dp).background(evaluationType.color.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(evaluationType.icon, contentDescription = null, tint = evaluationType.color, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(eval.title, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(eval.subjectName, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
        Spacer(modifier = Modifier.height(24.dp))
        
        if (!eval.imageUrl.isNullOrBlank()) {
            AsyncImage(model = eval.imageUrl, contentDescription = null, modifier = Modifier.fillMaxWidth().heightIn(max = 200.dp).clip(RoundedCornerShape(16.dp)), contentScale = ContentScale.Crop)
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        DetailRow(icon = Icons.AutoMirrored.Filled.Subject, label = "Descripción", content = eval.description)
        Spacer(modifier = Modifier.height(16.dp))
        DetailRow(icon = Icons.Default.Lightbulb, label = "Tips Rápidos", content = eval.quickTips, color = Color(0xFFFBC02D))

        if (evaluationType.showGrade && eval.grade != null) {
            Spacer(modifier = Modifier.height(16.dp))
            DetailRow(icon = Icons.Default.Event, label = "Nota Obtenida", content = "${eval.grade}/10", color = Aqua)
        }

        if (evaluationType.showWeight && eval.weight != null) {
            Spacer(modifier = Modifier.height(16.dp))
            DetailRow(icon = Icons.Default.Event, label = "Peso / Porcentaje", content = "${eval.weight}%", color = Aqua)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            FilledTonalIconButton(
                onClick = onEdit, 
                colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = Aqua.copy(alpha = 0.1f), contentColor = Aqua), 
                modifier = Modifier.size(48.dp)
            ) { 
                Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(20.dp)) 
            }
            Spacer(modifier = Modifier.width(16.dp))
            FilledTonalIconButton(
                onClick = onDelete, 
                colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = Ruby.copy(alpha = 0.1f), contentColor = Ruby), 
                modifier = Modifier.size(48.dp)
            ) { 
                Icon(Icons.Default.DeleteOutline, contentDescription = "Borrar", modifier = Modifier.size(20.dp)) 
            }
        }
    }
}

@Composable
fun DetailRow(icon: ImageVector, label: String, content: String, color: Color = Ruby) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(modifier = Modifier.padding(top = 2.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
            Spacer(modifier = Modifier.height(4.dp))
            Text(content.ifBlank { "Sin detalles" }, fontSize = 15.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun CalendarGrid(currentMonth: Calendar, selectedDate: Calendar, onDateSelected: (Calendar) -> Unit, evaluations: List<Evaluation>) {
    val daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = (currentMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }.get(Calendar.DAY_OF_WEEK) - 1
    val days = (1..daysInMonth).toList(); val paddingDays = (0 until firstDayOfWeek).map { "" }
    Column {
        Row(Modifier.fillMaxWidth()) {
            listOf("D", "L", "M", "X", "J", "V", "S").forEach { day ->
                Text(text = day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        val totalCells = paddingDays + days
        totalCells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                week.forEach { day ->
                    if (day is Int) {
                        val date = (currentMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, day) }
                        val isSelected = isSameDay(date.time, selectedDate.time)
                        val hasEvals = evaluations.any { isSameDay(it.date, date.time) }
                        val isToday = isSameDay(Date(), date.time)
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(CircleShape)
                                .background(if (isSelected) Ruby.copy(alpha = 0.12f) else Color.Transparent)
                                .clickable { onDateSelected(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                Text(
                                    text = day.toString(),
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Ruby else if (isToday) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                                // Dot for events
                                if (hasEvals) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Box(modifier = Modifier.size(4.dp).clip(CircleShape).background(if (isSelected) Ruby else Aqua))
                                } else {
                                    Spacer(modifier = Modifier.height(6.dp)) // To keep height consistent
                                }
                            }
                        }
                    } else Spacer(modifier = Modifier.weight(1f))
                }
                if (week.size < 7) repeat(7 - week.size) { Spacer(modifier = Modifier.weight(1f)) }
            }
        }
    }
}

fun isSameDay(date1: Date?, date2: Date): Boolean {
    if (date1 == null) return false
    val cal1 = Calendar.getInstance().apply { time = date1 }; val cal2 = Calendar.getInstance().apply { time = date2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
