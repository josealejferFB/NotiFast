package com.example.notifast.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.automirrored.filled.Subject
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Note
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.notifast.model.Evaluation
import com.example.notifast.model.Note
import com.example.notifast.ui.theme.Aqua
import com.example.notifast.ui.theme.Ruby
import com.example.notifast.viewmodel.NotesViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CalendarScreen(
    viewModel: NotesViewModel,
    onAddEvaluation: () -> Unit,
    onEditEvaluation: (Evaluation) -> Unit
) {
    val notes by viewModel.allNotes.collectAsState()
    val evaluations by viewModel.evaluations.collectAsState()
    
    var currentMonth by remember { mutableStateOf(Calendar.getInstance()) }
    var selectedDate by remember { mutableStateOf(Calendar.getInstance()) }
    
    // Estados de expansión
    var expandedEvalId by remember { mutableStateOf<String?>(null) }
    var expandedNoteId by remember { mutableStateOf<String?>(null) }
    
    val isHeaderVisible = expandedEvalId == null && expandedNoteId == null
    var showDeleteDialog by remember { mutableStateOf<Evaluation?>(null) }

    // Soporte para botón atrás físico
    BackHandler(enabled = !isHeaderVisible) {
        expandedEvalId = null
        expandedNoteId = null
    }

    if (showDeleteDialog != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("¿Eliminar evaluación?") },
            text = { Text("Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog?.let { viewModel.deleteEvaluation(it) }
                        showDeleteDialog = null
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = Ruby)
                ) { Text("Eliminar", fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Cancelar") }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = Color.White
        )
    }

    Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .padding(top = 16.dp)
        ) {
            Text(
                text = "Mi Calendario",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Header del Calendario Animado
            AnimatedVisibility(
                visible = isHeaderVisible,
                enter = fadeIn(tween(300)) + expandVertically(spring(stiffness = Spring.StiffnessLow)),
                exit = fadeOut(tween(200)) + shrinkVertically(spring(stiffness = Spring.StiffnessLow))
            ) {
                Column {
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
                            text = SimpleDateFormat("MMMM yyyy", Locale("es", "ES")).format(currentMonth.time).replaceFirstChar { it.uppercase() },
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )

                        IconButton(onClick = {
                            val newMonth = currentMonth.clone() as Calendar
                            newMonth.add(Calendar.MONTH, 1)
                            currentMonth = newMonth
                        }) {
                            Icon(Icons.Default.ChevronRight, contentDescription = null)
                        }
                    }

                    CalendarGrid(
                        currentMonth = currentMonth,
                        selectedDate = selectedDate,
                        onDateSelected = { 
                            selectedDate = it 
                            expandedEvalId = null
                            expandedNoteId = null
                        },
                        notes = notes,
                        evaluations = evaluations
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            Text(
                text = if (isHeaderVisible) "Eventos del día" else "Detalle del elemento",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                modifier = Modifier.padding(vertical = 8.dp)
            )

            val dailyNotes = notes.filter { isSameDay(it.timestamp, selectedDate.time) }
            val dailyEvals = evaluations.filter { isSameDay(it.date, selectedDate.time) }

            Box(modifier = Modifier.weight(1f)) {
                if (dailyNotes.isEmpty() && dailyEvals.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No hay actividades para este día", color = Color.Gray)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        // SECCIÓN EVALUACIONES
                        items(dailyEvals) { eval ->
                            if (expandedNoteId == null) {
                                EvaluationExpandableItem(
                                    eval = eval,
                                    isExpanded = expandedEvalId == eval.id,
                                    onToggle = { 
                                        expandedEvalId = if (expandedEvalId == eval.id) null else eval.id 
                                    },
                                    onEdit = { onEditEvaluation(eval) },
                                    onDelete = { showDeleteDialog = eval }
                                )
                            }
                        }
                        
                        // SECCIÓN NOTAS (AHORA EXPANDIBLES TAMBIÉN)
                        items(dailyNotes) { note ->
                            if (expandedEvalId == null) {
                                NoteExpandableItem(
                                    note = note,
                                    isExpanded = expandedNoteId == note.id,
                                    onToggle = {
                                        expandedNoteId = if (expandedNoteId == note.id) null else note.id
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (isHeaderVisible) {
            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.BottomEnd) {
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
}

@Composable
fun NoteExpandableItem(
    note: Note,
    isExpanded: Boolean,
    onToggle: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 8.dp else 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(interactionSource = interactionSource, indication = null) { onToggle() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).background(Aqua.copy(alpha = 0.08f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Note, contentDescription = null, tint = Aqua)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(note.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(note.subject, fontSize = 13.sp, color = Color.Gray)
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (!note.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = note.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth().wrapContentHeight().clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.FillWidth
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    Text(text = note.content, style = MaterialTheme.typography.bodyLarge, lineHeight = 24.sp)
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    TextButton(onClick = onToggle, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cerrar detalle", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun EvaluationExpandableItem(
    eval: Evaluation,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isExpanded) 8.dp else 0.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(interactionSource = interactionSource, indication = null) { onToggle() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp).background(Ruby.copy(alpha = 0.08f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Event, contentDescription = null, tint = Ruby)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(eval.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(eval.subjectName, fontSize = 13.sp, color = Color.Gray)
                }
            }

            AnimatedVisibility(
                visible = isExpanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (!eval.imageUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = eval.imageUrl,
                            contentDescription = null,
                            modifier = Modifier.fillMaxWidth().wrapContentHeight().clip(RoundedCornerShape(16.dp)),
                            contentScale = ContentScale.FillWidth
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    DetailRow(icon = Icons.AutoMirrored.Filled.Subject, label = "Descripción", content = eval.description)
                    Spacer(modifier = Modifier.height(12.dp))
                    DetailRow(icon = Icons.Default.Lightbulb, label = "Tips Rápidos", content = eval.quickTips, color = Color(0xFFFBC02D))
                    
                    Spacer(modifier = Modifier.height(20.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        TextButton(onClick = onToggle) {
                            Icon(Icons.Default.KeyboardArrowUp, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Gray)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Cerrar", color = Color.Gray)
                        }

                        Row {
                            FilledTonalIconButton(
                                onClick = onEdit,
                                colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = Aqua.copy(alpha = 0.1f), contentColor = Aqua),
                                modifier = Modifier.size(40.dp)
                            ) { Icon(Icons.Default.Edit, contentDescription = "Editar", modifier = Modifier.size(18.dp)) }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            FilledTonalIconButton(
                                onClick = onDelete,
                                colors = IconButtonDefaults.filledTonalIconButtonColors(containerColor = Ruby.copy(alpha = 0.1f), contentColor = Ruby),
                                modifier = Modifier.size(40.dp)
                            ) { Icon(Icons.Default.DeleteOutline, contentDescription = "Borrar", modifier = Modifier.size(18.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, content: String, color: Color = Ruby) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
            Text(content.ifBlank { "Sin detalles" }, fontSize = 14.sp)
        }
    }
}

@Composable
fun CalendarGrid(currentMonth: Calendar, selectedDate: Calendar, onDateSelected: (Calendar) -> Unit, notes: List<Note>, evaluations: List<Evaluation>) {
    val daysInMonth = currentMonth.getActualMaximum(Calendar.DAY_OF_MONTH)
    val firstDayOfWeek = (currentMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }.get(Calendar.DAY_OF_WEEK) - 1
    val days = (1..daysInMonth).toList()
    val paddingDays = (0 until firstDayOfWeek).toList()

    Column {
        Row(Modifier.fillMaxWidth()) {
            listOf("D", "L", "M", "X", "J", "V", "S").forEach { day ->
                Text(text = day, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontWeight = FontWeight.Bold, color = Color.Gray, fontSize = 12.sp)
            }
        }
        val totalCells = paddingDays + days
        totalCells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                week.forEach { day ->
                    if (day is Int) {
                        val date = (currentMonth.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, day) }
                        val isSelected = isSameDay(date.time, selectedDate.time)
                        val hasNotes = notes.any { isSameDay(it.timestamp, date.time) }
                        val hasEvals = evaluations.any { isSameDay(it.date, date.time) }
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f).clip(RoundedCornerShape(12.dp)).background(if (isSelected) Ruby.copy(alpha = 0.2f) else Color.Transparent).clickable { onDateSelected(date) }, contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = day.toString(), fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = if (isSelected) Ruby else MaterialTheme.colorScheme.onSurface)
                                Row {
                                    if (hasNotes) Box(Modifier.size(4.dp).background(Aqua, CircleShape))
                                    if (hasEvals) { if (hasNotes) Spacer(Modifier.width(2.dp)); Box(Modifier.size(4.dp).background(Ruby, CircleShape)) }
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

@Composable
fun EventItem(title: String, subtitle: String, color: Color, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Card(shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)), modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = color)
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold)
                Text(subtitle, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

fun isSameDay(date1: Date?, date2: Date): Boolean {
    if (date1 == null) return false
    val cal1 = Calendar.getInstance().apply { time = date1 }; val cal2 = Calendar.getInstance().apply { time = date2 }
    return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) && cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR)
}
