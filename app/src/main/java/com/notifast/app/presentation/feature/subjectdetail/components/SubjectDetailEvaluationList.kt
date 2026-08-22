package com.notifast.app.presentation.feature.subjectdetail.components

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.notifast.app.data.model.Evaluation
import com.notifast.app.presentation.theme.color.Aqua
import com.notifast.app.presentation.theme.color.Ruby
import java.util.Calendar
import java.util.Date

private fun parseSubjectColor(hex: String): Color {
    return try {
        if (hex.isNotBlank()) Color(android.graphics.Color.parseColor(hex))
        else Aqua
    } catch (e: Exception) { Aqua }
}

private fun getMonthNameShort(date: Date?): String {
    if (date == null) return "S/F"
    val cal = Calendar.getInstance().apply { time = date }
    val months = arrayOf("ENE", "FEB", "MAR", "ABR", "MAY", "JUN", "JUL", "AGO", "SEP", "OCT", "NOV", "DIC")
    return months[cal.get(Calendar.MONTH)]
}

private fun getDay(date: Date?): String {
    if (date == null) return "-"
    val cal = Calendar.getInstance().apply { time = date }
    return cal.get(Calendar.DAY_OF_MONTH).toString()
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SubjectDetailEvaluationList(
    evaluations: List<Evaluation>,
    subjectColorHex: String,
    onMarkComplete: (Evaluation) -> Unit = {},
    onEditGrade: (Evaluation) -> Unit = {},
    onDeleteEval: (Evaluation) -> Unit = {}
) {
    if (evaluations.isEmpty()) return

    val color = parseSubjectColor(subjectColorHex)

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) {
        evaluations.forEach { eval ->
            var showMenu by remember { mutableStateOf(false) }
            val isCompleted = eval.isPresented

            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().combinedClickable(
                        onClick = {},
                        onLongClick = { showMenu = true }
                    ).padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Calendar Sheet Aesthetic
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, color.copy(alpha = 0.2f)),
                        modifier = Modifier.width(64.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(color)
                                    .padding(vertical = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = getMonthNameShort(eval.date),
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = getDay(eval.date),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 6.dp)
                            )
                        }
                    }
                    
                    Spacer(Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = eval.title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isCompleted) {
                                Surface(shape = RoundedCornerShape(6.dp), color = color.copy(alpha = 0.15f)) {
                                    Text(
                                        text = eval.grade?.let { String.format("%.1f", it) } ?: "\u2014",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = color
                                    )
                                }
                            } else {
                                Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.surfaceVariant) {
                                    Text(
                                        text = "Pendiente",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            val weight = eval.weight ?: 0.0
                            if (weight > 0.0) {
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "${(weight * 100).toInt()}% peso",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    if (!isCompleted) {
                        DropdownMenuItem(
                            text = { Text("Marcar completada") },
                            onClick = { showMenu = false; onMarkComplete(eval) },
                            leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Aqua) }
                        )
                    } else {
                        DropdownMenuItem(
                            text = { Text("Editar nota") },
                            onClick = { showMenu = false; onEditGrade(eval) },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null, tint = Aqua) }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Eliminar", color = Ruby) },
                        onClick = { showMenu = false; onDeleteEval(eval) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Ruby) }
                    )
                }
            }
        }
    }
}
