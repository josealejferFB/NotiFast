package com.example.notifast.presentation.feature.grades

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Pending
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.notifast.data.model.Evaluation
import com.example.notifast.domain.util.GradeConstants
import com.example.notifast.presentation.atoms.NotiFastCircularLoading
import com.example.notifast.presentation.atoms.NotiFastCenterAlignedTopBar
import com.example.notifast.presentation.atoms.NotiFastStudyBackground
import com.example.notifast.presentation.theme.color.Aqua
import com.example.notifast.presentation.theme.color.Ruby
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradesScreen(
    viewModel: GradesViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.isLoading) {
        NotiFastCircularLoading()
        return
    }

    val allSubjects = uiState.evaluationsBySubject.filter { it.value.isNotEmpty() }.keys.toList().sorted()

    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastStudyBackground()
        Scaffold(containerColor = Color.Transparent, topBar = { NotiFastCenterAlignedTopBar(title = "Rendimiento") }) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding).safeDrawingPadding()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp)
            ) {
                item {
                    MainAverageDashboard(uiState.globalAverage)
                }

                if (uiState.subjectGrades.isEmpty()) {
                    item {
                        EmptyGradesState()
                    }
                } else {
                    items(uiState.subjectGrades) { subjectGrade ->
                        SubjectRevampCard(
                            subjectGrade = subjectGrade,
                            onUpdateGrade = { id, g, p ->
                                viewModel.onEvent(GradesEvent.UpdateGrade(id, g, p))
                            }
                        )
                    }
                }
            }
        }
    }
    }
}

@Composable
fun MainAverageDashboard(generalAverage: Double) {

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
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

                val maxScale = GradeConstants.MAX_GRADE.toFloat()
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

                val threshold = GradeConstants.PASSING_THRESHOLD
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
    subjectGrade: SubjectGradeInfo,
    onUpdateGrade: (String, Double?, Boolean) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val subject = subjectGrade.subject
    val evaluations = subjectGrade.evaluations
    val subjectAverage = subjectGrade.average

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
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
                        Text(subject, fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, maxLines = 1, color = MaterialTheme.colorScheme.onSurface)
                        Text("${evaluations.size} evaluaciones", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    val threshold = GradeConstants.PASSING_THRESHOLD
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
fun EvaluationDetailRow(evaluation: Evaluation, onUpdateGrade: (String, Double?, Boolean) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
            .clickable { showDialog = true },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(evaluation.title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    if (evaluation.isPresented) Icons.Rounded.CheckCircle else Icons.Rounded.Pending,
                    null,
                    tint = if (evaluation.isPresented) Aqua else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (evaluation.isPresented) "Completada" else "Pendiente",
                    fontSize = 11.sp,
                    color = if (evaluation.isPresented) Aqua else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Surface(
            color = if (evaluation.grade != null) Ruby.copy(alpha = 0.1f) else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
            shape = RoundedCornerShape(12.dp)
        ) {
            val gradeStr = evaluation.grade?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "—"
            Text(
                text = gradeStr,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                fontWeight = FontWeight.Bold,
                color = if (evaluation.grade != null) Ruby else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }

    if (showDialog) {
        GradeRevampDialog(evaluation, onDismiss = { showDialog = false }, onSave = onUpdateGrade)
    }
}

@Composable
fun GradeRevampDialog(
    evaluation: Evaluation,
    onDismiss: () -> Unit,
    onSave: (String, Double?, Boolean) -> Unit
) {
    var grade by remember { mutableStateOf(evaluation.grade?.let { if (it % 1.0 == 0.0) it.toInt().toString() else it.toString() } ?: "") }
    var isPresented by remember { mutableStateOf(evaluation.isPresented) }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(32.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        title = { Text("Registrar Resultado", fontWeight = FontWeight.Black, color = Ruby) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(evaluation.title, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)

                OutlinedTextField(
                    value = grade,
                    onValueChange = {
                        grade = it
                        error = null
                        val parsed = it.toDoubleOrNull()
                        if (it.isNotEmpty() && parsed != null && parsed in GradeConstants.MIN_GRADE..GradeConstants.MAX_GRADE) {
                            isPresented = true
                        }
                    },
                    label = { Text("Nota (${GradeConstants.MIN_GRADE} - ${GradeConstants.MAX_GRADE})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    isError = error != null,
                    supportingText = error?.let { { Text(it, color = Ruby) } },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { isPresented = !isPresented }
                        .padding(vertical = 8.dp)
                ) {
                    Checkbox(
                        checked = isPresented,
                        onCheckedChange = { isPresented = it },
                        colors = CheckboxDefaults.colors(checkedColor = Aqua)
                    )
                    Text("Marcar como presentada", fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (grade.isBlank()) {
                        onSave(evaluation.id, null, false)
                        onDismiss()
                    } else {
                        val g = grade.toDoubleOrNull()
                        when {
                            g == null -> error = "Nota inválida"
                            g < GradeConstants.MIN_GRADE || g > GradeConstants.MAX_GRADE -> error = "La nota debe estar entre ${GradeConstants.MIN_GRADE} y ${GradeConstants.MAX_GRADE}"
                            else -> {
                                onSave(evaluation.id, g, isPresented)
                                onDismiss()
                            }
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Ruby)
            ) {
                Text("GUARDAR", fontWeight = FontWeight.Bold)
            }
        }
    )
}

@Composable
fun EmptyGradesState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Rounded.Info, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f), modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Aún no tienes evaluaciones\nregistradas.",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
