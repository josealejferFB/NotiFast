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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.notifast.data.model.Evaluation
import com.example.notifast.presentation.theme.color.Aqua
import com.example.notifast.presentation.theme.color.Ruby

private fun parseSubjectColor(hex: String): Color {
    return try {
        if (hex.isNotBlank()) Color(android.graphics.Color.parseColor(hex))
        else Aqua
    } catch (e: Exception) { Aqua }
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

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
        Spacer(Modifier.height(16.dp))
        Text("Todas las evaluaciones", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))

        evaluations.forEach { eval ->
            var showMenu by remember { mutableStateOf(false) }
            val isCompleted = eval.isPresented

            Card(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().combinedClickable(
                        onClick = {},
                        onLongClick = { showMenu = true }
                    ).padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier.size(10.dp).clip(CircleShape).background(
                            if (isCompleted) Color(0xFF43A047) else color.copy(alpha = 0.3f)
                        )
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(eval.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isCompleted) {
                                Surface(shape = RoundedCornerShape(6.dp), color = Aqua.copy(alpha = 0.1f)) {
                                    Text(
                                        text = eval.grade?.let { String.format("%.1f", it) } ?: "\u2014",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = Aqua
                                    )
                                }
                            } else {
                                Text("Pendiente", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            val weight = eval.weight ?: 0.0
                            if (weight > 0.0) {
                                Spacer(Modifier.width(6.dp))
                                Text("${(weight * 100).toInt()}%", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
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
