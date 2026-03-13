package com.example.notifast.presentation.ui.grades.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.notifast.data.model.Evaluation
import com.example.notifast.presentation.theme.color.Aqua
import com.example.notifast.presentation.theme.color.Ruby
import java.util.*

@Composable
fun MainAverageDashboard(evaluationsBySubject: Map<String, List<Evaluation>>) {
    val subjectAverages = evaluationsBySubject.values.mapNotNull { evaluations ->
        val graded = evaluations.filter { it.grade != null }
        if (graded.isNotEmpty()) {
            graded.map { it.grade!! }.average()
        } else null
    }

    val generalAverage = if (subjectAverages.isNotEmpty()) subjectAverages.average() else 0.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Ruby, Ruby.copy(alpha = 0.8f))
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "PROMEDIO GLOBAL",
                    color = Color.White.copy(alpha = 0.7f),
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp,
                    fontSize = 12.sp
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = String.format(Locale.getDefault(), "%.2f", generalAverage),
                    color = Color.White,
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.displayLarge
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                val maxScale = 10f
                LinearProgressIndicator(
                    progress = { (generalAverage.toFloat() / maxScale).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(CircleShape),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                val threshold = 6.0
                Text(
                    text = if (generalAverage >= threshold) "¡Excelente trabajo!" else "¡Sigue esforzándote!",
                    color = Color.White,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun SubjectRevampCard(
    subject: String,
    evaluations: List<Evaluation>,
    onUpdateGrade: (String, Double, Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    
    val graded = evaluations.filter { it.grade != null }
    val subjectAverage = if (graded.isNotEmpty()) graded.map { it.grade!! }.average() else 0.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.8f)),
        border = BorderStroke(1.dp, if (expanded) Ruby.copy(alpha = 0.2f) else Color.Transparent)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        color = Aqua.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(Icons.Rounded.School, null, tint = Aqua, modifier = Modifier.padding(14.dp))
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(subject, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, maxLines = 1)
                        Text("${evaluations.size} evaluaciones", fontSize = 12.sp, color = Color.Gray)
                    }
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    val threshold = 6.0
                    Text(
                        text = String.format(Locale.getDefault(), "%.1f", subjectAverage),
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        color = if (subjectAverage >= threshold) Aqua else Ruby
                    )
                }
            }

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 20.dp)) {
                    HorizontalDivider(color = Color.LightGray.copy(alpha = 0.3f))
                    Spacer(modifier = Modifier.height(8.dp))
                    evaluations.forEach { eval ->
                        EvaluationDetailRow(eval, onUpdateGrade)
                    }
                }
            }
        }
    }
}

@Composable
fun EvaluationDetailRow(evaluation: Evaluation, onUpdateGrade: (String, Double, Boolean) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .clickable { showDialog = true },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(evaluation.title, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (evaluation.isPresented) Icons.Rounded.CheckCircle else Icons.Rounded.Pending,
                    null,
                    tint = if (evaluation.isPresented) Aqua else Color.Gray,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (evaluation.isPresented) "Completada" else "Pendiente",
                    fontSize = 11.sp,
                    color = if (evaluation.isPresented) Aqua else Color.Gray
                )

            }
        }
        
        Surface(
            color = if (evaluation.grade != null) Ruby.copy(alpha = 0.1f) else Color.LightGray.copy(alpha = 0.2f),
            shape = RoundedCornerShape(12.dp)
        ) {
            val gradeStr = evaluation.grade?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "—"
            Text(
                text = gradeStr,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                fontWeight = FontWeight.Bold,
                color = if (evaluation.grade != null) Ruby else Color.Gray
            )
        }
    }

    if (showDialog) {
        GradeRevampDialog(evaluation, onDismiss = { showDialog = false }, onSave = onUpdateGrade)
    }
}

@Composable
fun EmptyGradesState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Rounded.Info, null, tint = Color.LightGray, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Aún no tienes evaluaciones\nregistradas.",
            textAlign = TextAlign.Center,
            color = Color.Gray,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
