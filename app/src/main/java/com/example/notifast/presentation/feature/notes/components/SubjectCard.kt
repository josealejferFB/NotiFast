package com.example.notifast.presentation.feature.notes.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.notifast.data.model.Evaluation
import com.example.notifast.data.model.Note
import com.example.notifast.presentation.feature.notes.SubjectInfo
import com.example.notifast.presentation.theme.color.Aqua
import com.example.notifast.presentation.theme.color.Ruby
import java.util.Date

private fun getSubjectColor(info: SubjectInfo): Color {
    return try {
        if (info.colorHex.isNotBlank()) Color(android.graphics.Color.parseColor(info.colorHex))
        else {
            val hash = info.name.hashCode()
            val hue = ((hash and 0xFF).toFloat() / 255f * 360f)
            Color.hsl(hue, 0.7f, 0.55f)
        }
    } catch (e: Exception) {
        Aqua
    }
}

private fun getInitials(name: String): String {
    return name.trim().split(" ")
        .mapNotNull { it.firstOrNull()?.toString() }
        .take(2)
        .joinToString("")
        .uppercase()
}

private fun daysUntil(date: Date): Long {
    val diff = date.time - System.currentTimeMillis()
    return diff / (1000 * 60 * 60 * 24)
}

private fun urgencyColor(days: Long): Color {
    return when {
        days < 0 -> Color.Gray
        days <= 7 -> Color(0xFFE53935)
        days <= 14 -> Color(0xFFFB8C00)
        else -> Color(0xFF43A047)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SubjectCard(
    info: SubjectInfo,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onAddNote: (String) -> Unit,
    onAddEvaluation: (String) -> Unit,
    onNoteClick: (Note) -> Unit,
    onDeleteSubject: () -> Unit
) {
    val color = getSubjectColor(info)
    val initials = getInitials(info.name)
    var showMenu by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "chevron"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Column {
            // Compact header (always visible)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = { onToggle() },
                        onLongClick = { showMenu = true }
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Colored left bar logic incorporated into padding if desired, or here as a colored box
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(color),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = SubjectIconMapper.getIcon(info.icon),
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(info.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "${info.notes.size} apuntes · ${info.evaluations.size} evaluaciones" + if(info.pendingEvalCount > 0) " · ${info.pendingEvalCount} pdts" else "",
                        style = MaterialTheme.typography.bodySmall, 
                        color = Color.Gray
                    )
                }
                if (info.averageGrade != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            color = color.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = String.format("%.1f", info.averageGrade), 
                                style = MaterialTheme.typography.titleMedium, 
                                fontWeight = FontWeight.ExtraBold, 
                                color = color,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                } else {
                    Spacer(Modifier.width(8.dp))
                }
                Spacer(Modifier.width(8.dp))
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp).graphicsLayer { rotationZ = rotation },
                    tint = Color.Gray
                )
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Eliminar materia", color = Color(0xFFE53935)) },
                    onClick = {
                        showMenu = false
                        onDeleteSubject()
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFE53935))
                    }
                )
            }

            // Expanded content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = spring(dampingRatio = 0.6f, stiffness = 280f)) + fadeIn(animationSpec = spring(dampingRatio = 0.6f, stiffness = 280f)),
                exit = shrinkVertically(animationSpec = spring(dampingRatio = 0.6f, stiffness = 280f)) + fadeOut(animationSpec = spring(dampingRatio = 0.6f, stiffness = 280f))
            ) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    Spacer(Modifier.height(16.dp))

                    // Upcoming evaluations
                    if (info.upcomingEvaluations.isNotEmpty()) {
                        Text("Próximas evaluaciones", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        info.upcomingEvaluations.forEach { eval ->
                            val days = eval.date?.let { daysUntil(it) } ?: 999
                            UrgencyCard(eval = eval, days = days, color = color)
                            Spacer(Modifier.height(8.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    // Dashboard: average + stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Estadísticas", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.Gray)
                            Spacer(Modifier.height(8.dp))
                            Text("${info.evaluations.size} totales", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                            Text("${info.completedEvalCount} completadas", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                            Text("${info.pendingEvalCount} pendientes", style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
                        }
                        Box(
                            modifier = Modifier.size(64.dp).clip(CircleShape).background(color.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = info.averageGrade?.let { String.format("%.1f", it) } ?: "—",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = color
                                )
                                Text("Promedio", style = MaterialTheme.typography.labelSmall, color = color.copy(alpha = 0.8f))
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Recent notes mini cards
                    if (info.notes.isNotEmpty()) {
                        Text("Últimos apuntes", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.Gray)
                        Spacer(Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            info.notes.take(3).forEach { note ->
                                MiniNoteCard(note = note, color = color, onClick = { onNoteClick(note) })
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                    }

                    // Quick actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onAddNote(info.name) },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, color),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = color)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Apunte", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { onAddEvaluation(info.name) },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = color)
                        ) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Evaluación", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UrgencyCard(eval: Evaluation, days: Long, color: Color) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val chipColor = urgencyColor(days)
            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(chipColor))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(eval.title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val weight = eval.weight ?: 0.0
                    if (weight > 0.0) {
                        Text("${(weight * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                        Text(" · ", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                    Text(eval.date?.let { formatDate(it) } ?: "Sin fecha", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                }
            }
            Text(
                text = when {
                    days < 0 -> "Vencida"
                    days == 0L -> "Hoy"
                    days == 1L -> "Mañana"
                    else -> "${days}d"
                },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = chipColor
            )
        }
    }
}

@Composable
private fun MiniNoteCard(note: Note, color: Color, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.15f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(24.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(color)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                text = note.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (note.imageUrl != null) {
                Spacer(Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.White.copy(alpha = 0.4f))
                ) {
                    SubcomposeAsyncImage(
                        model = note.imageUrl,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

private fun formatDate(date: Date): String {
    val cal = java.util.Calendar.getInstance().apply { time = date }
    return "${cal.get(java.util.Calendar.DAY_OF_MONTH)}/${cal.get(java.util.Calendar.MONTH) + 1}"
}
