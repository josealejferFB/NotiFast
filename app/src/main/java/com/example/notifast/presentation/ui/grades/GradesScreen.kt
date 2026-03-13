package com.example.notifast.presentation.ui.grades

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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

import com.example.notifast.presentation.common.viewmodel.NotesViewModel
import com.example.notifast.presentation.theme.color.Aqua
import com.example.notifast.presentation.theme.color.Ruby
import com.example.notifast.presentation.ui.grades.components.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradesScreen(viewModel: NotesViewModel) {
    val evaluationsBySubject by viewModel.evaluationsBySubject.collectAsState()
    val subjects by viewModel.subjects.collectAsState()

    val allSubjects = evaluationsBySubject.filter { it.value.isNotEmpty() }.keys.toList().sorted()

    Box(modifier = Modifier.fillMaxSize().safeDrawingPadding()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 40.dp)
        ) {
            item {
                Text(
                    text = "Rendimiento",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            }
            item {
                MainAverageDashboard(evaluationsBySubject)
            }

            if (allSubjects.isEmpty()) {
                item {
                    EmptyGradesState()
                }
            } else {
                items(allSubjects) { subject ->
                    SubjectRevampCard(
                        subject = subject,
                        evaluations = evaluationsBySubject[subject] ?: emptyList(),
                        onUpdateGrade = { id, g, p ->
                            viewModel.updateEvaluationGradeAndWeight(id, g, null, p)
                        }
                    )
                }
            }
        }
    }
}

