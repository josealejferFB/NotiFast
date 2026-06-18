package com.example.notifast.presentation.feature.subjectdetail.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notifast.data.model.Evaluation
import com.example.notifast.presentation.theme.color.Aqua
import com.example.notifast.presentation.theme.color.Ruby
import java.util.Date

private fun parseSubjectColor(hex: String): Color {
    return try {
        if (hex.isNotBlank()) Color(android.graphics.Color.parseColor(hex))
        else Aqua
    } catch (e: Exception) { Aqua }
}

private fun daysUntil(date: Date): Long {
    val diff = date.time - System.currentTimeMillis()
    return diff / (1000 * 60 * 60 * 24)
}

private fun urgencyColor(days: Long): Color = when {
    days < 0 -> Color.Gray
    days <= 7 -> Color(0xFFE53935)
    days <= 14 -> Color(0xFFFB8C00)
    else -> Color(0xFF43A047)
}

private fun formatDate(date: Date): String {
    val cal = java.util.Calendar.getInstance().apply { time = date }
    val months = listOf("ene", "feb", "mar", "abr", "may", "jun", "jul", "ago", "sep", "oct", "nov", "dic")
    return "${cal.get(java.util.Calendar.DAY_OF_MONTH)} ${months[cal.get(java.util.Calendar.MONTH)]}"
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SubjectDetailTimeline(
    upcomingEvaluations: List<Evaluation>,
    subjectColorHex: String,
    onMarkComplete: (Evaluation) -> Unit = {},
    onDeleteEval: (Evaluation) -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
        Spacer(Modifier.height(16.dp))
        Text("Próximas evaluaciones", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        if (upcomingEvaluations.isEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFFE8F5E9)
            ) {
                Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(24.dp).clip(CircleShape).background(Color(0xFF43A047)), contentAlignment = Alignment.Center) {
                        Text("\u2713", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Text("\u00A1Todo al d\u00EDa!", fontWeight = FontWeight.Medium, color = Color(0xFF2E7D32))
                }
            }
        } else {
            val color = parseSubjectColor(subjectColorHex)
            Column {
                upcomingEvaluations.forEachIndexed { index, eval ->
                    val days = eval.date?.let { daysUntil(it) } ?: 999
                    val chipColor = urgencyColor(days)
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(24.dp)) {
                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(chipColor))
                            if (index < upcomingEvaluations.lastIndex) {
                                Box(modifier = Modifier.width(2.dp).height(50.dp).background(color.copy(alpha = 0.3f)))
                            }
                        }
                        Spacer(Modifier.width(12.dp))
                        Box(modifier = Modifier.weight(1f).padding(bottom = 8.dp)) {
                            var showMenu by remember { mutableStateOf(false) }
                            Surface(
                                modifier = Modifier.fillMaxWidth().combinedClickable(
                                    onClick = {},
                                    onLongClick = { showMenu = true }
                                ),
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White,
                                shadowElevation = 1.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(eval.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(eval.date?.let { formatDate(it) } ?: "Sin fecha", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                            val weight = eval.weight ?: 0.0
                                            if (weight > 0.0) {
                                                Text(" · ${(weight * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                                            }
                                        }
                                    }
                                    Box(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp).clip(RoundedCornerShape(6.dp)).background(chipColor.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = when {
                                                days < 0 -> "Vencida"
                                                days == 0L -> "Hoy"
                                                days == 1L -> "Ma\u00F1ana"
                                                else -> "${days}d"
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = chipColor
                                        )
                                    }
                                }
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Marcar completada") },
                                    onClick = {
                                        showMenu = false
                                        onMarkComplete(eval)
                                    },
                                    leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Aqua) }
                                )
                                DropdownMenuItem(
                                    text = { Text("Eliminar", color = Ruby) },
                                    onClick = {
                                        showMenu = false
                                        onDeleteEval(eval)
                                    },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Ruby) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
