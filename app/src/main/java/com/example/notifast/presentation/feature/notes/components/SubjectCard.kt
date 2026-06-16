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
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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

private fun subjectColor(name: String): Color {
    val hash = name.hashCode()
    val hue = ((hash and 0xFF).toFloat() / 255f * 360f)
    return Color.hsl(hue, 0.7f, 0.55f)
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

// Converts a grade from 0–10 scale to a 0–5 star rating
private fun starRating(avg: Double, maxStars: Int = 5): String {
    val filled = (avg / 2.0).toInt().coerceIn(0, maxStars)
    val empty = maxStars - filled
    return "★".repeat(filled) + "☆".repeat(empty)
}

@Composable
fun SubjectCard(
    info: SubjectInfo,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    onAddNote: (String) -> Unit,
    onAddEvaluation: (String) -> Unit
) {
    val color = subjectColor(info.name)
    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 180f else 0f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "chevron"
    )

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column {
            // Compact header (always visible)
            Row(
                modifier = Modifier.fillMaxWidth().clickable { onToggle() }.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(color),
                    contentAlignment = Alignment.Center
                ) {
                    Text(info.icon, fontSize = 22.sp)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(info.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("📝 ${info.notes.size}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text(" · ", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text("📅 ${info.evaluations.size}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        if (info.pendingEvalCount > 0) {
                            Text(" · ", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Text("⏳ ${info.pendingEvalCount}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }
                }
                if (info.averageGrade != null) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(starRating(info.averageGrade), color = Color(0xFFFFB300), fontSize = 14.sp)
                        Text(String.format("%.1f", info.averageGrade), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
                    }
                }
                Spacer(Modifier.width(8.dp))
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp).graphicsLayer { rotationZ = rotation },
                    tint = Color.Gray
                )
            }

            // Expanded content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(animationSpec = spring(dampingRatio = 0.6f, stiffness = 280f)) + fadeIn(animationSpec = spring(dampingRatio = 0.6f, stiffness = 280f)),
                exit = shrinkVertically(animationSpec = spring(dampingRatio = 0.6f, stiffness = 280f)) + fadeOut(animationSpec = spring(dampingRatio = 0.6f, stiffness = 280f))
            ) {
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    // Upcoming evaluations
                    if (info.upcomingEvaluations.isNotEmpty()) {
                        Text("📅 Próximas evaluaciones", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        info.upcomingEvaluations.forEach { eval ->
                            val days = eval.date?.let { daysUntil(it) } ?: 999
                            UrgencyCard(eval = eval, days = days, color = color)
                            Spacer(Modifier.height(6.dp))
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    Spacer(Modifier.height(12.dp))

                    // Dashboard: average + stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Circular score
                        Box(
                            modifier = Modifier.size(72.dp).clip(CircleShape).background(color.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = info.averageGrade?.let { String.format("%.1f", it) } ?: "—",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = color
                            )
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Promedio", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                            Text("📝 ${info.evaluations.size} evaluaciones", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Text("✅ ${info.completedEvalCount} completadas", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            Text("⏳ ${info.pendingEvalCount} pendientes", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    Spacer(Modifier.height(12.dp))

                    // Recent notes mini cards
                    if (info.notes.isNotEmpty()) {
                        Text("📝 Últimos apuntes", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(info.notes.take(3)) { note ->
                                MiniNoteCard(note = note, color = color)
                            }
                            if (info.notes.size > 3) {
                                item {
                                    Surface(
                                        modifier = Modifier.size(width = 100.dp, height = 80.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        color = color.copy(alpha = 0.05f),
                                        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("+${info.notes.size - 3}", fontWeight = FontWeight.Bold, color = color)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Quick actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = { onAddNote(info.name) },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Ruby)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Apunte", fontWeight = FontWeight.Bold)
                        }
                        Button(
                            onClick = { onAddEvaluation(info.name) },
                            modifier = Modifier.weight(1f).height(44.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Aqua)
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
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(eval.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val weight = eval.weight ?: 0.0
                    if (weight > 0.0) {
                        Text("🏷️ ${(weight * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Text(" · ", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                    }
                    Text("📆 ${eval.date?.let { formatDate(it) } ?: "Sin fecha"}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
            val chipColor = urgencyColor(days)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = chipColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = when {
                        days < 0 -> "Vencida"
                        days == 0L -> "Hoy"
                        days == 1L -> "Mañana"
                        else -> "en ${days}días"
                    },
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    color = chipColor
                )
            }
        }
    }
}

@Composable
private fun MiniNoteCard(note: Note, color: Color) {
    Surface(
        modifier = Modifier.size(width = 100.dp, height = 80.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            if (note.imageUrl != null) {
                Box(modifier = Modifier.fillMaxWidth().height(32.dp).clip(RoundedCornerShape(6.dp)).background(color.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    SubcomposeAsyncImage(model = note.imageUrl, contentDescription = null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
                Spacer(Modifier.height(4.dp))
            }
            Text(note.title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
    }
}

private fun formatDate(date: Date): String {
    val cal = java.util.Calendar.getInstance().apply { time = date }
    return "${cal.get(java.util.Calendar.DAY_OF_MONTH)}/${cal.get(java.util.Calendar.MONTH) + 1}"
}
