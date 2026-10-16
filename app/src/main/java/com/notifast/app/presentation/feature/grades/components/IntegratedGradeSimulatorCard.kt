package com.notifast.app.presentation.feature.grades.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.domain.usecase.CalculateRequiredGradeUseCase
import com.notifast.app.presentation.atoms.NotiFastCard
import com.notifast.app.presentation.feature.grades.SubjectGradeInfo

/**
 * Interactive grade simulator section integrated into Grades Hub.
 * Empowers students to pick any subject and simulate required grades in real time.
 */
@Composable
fun IntegratedGradeSimulatorCard(
    subjectGrades: List<SubjectGradeInfo>,
    gradeScale: GradeScale,
    minPassingGrade: Double = gradeScale.minPassingGrade,
    modifier: Modifier = Modifier
) {
    if (subjectGrades.isEmpty()) return

    val haptic = LocalHapticFeedback.current
    val calculateRequiredGrade = remember { CalculateRequiredGradeUseCase() }

    var selectedSubjectName by remember(subjectGrades) {
        mutableStateOf(subjectGrades.firstOrNull()?.subject ?: "")
    }

    var targetGrade by remember(gradeScale, minPassingGrade) {
        mutableDoubleStateOf(minPassingGrade)
    }

    val selectedSubject = subjectGrades.firstOrNull {
        it.subject.equals(selectedSubjectName, ignoreCase = true)
    } ?: subjectGrades.first()

    val simulationResult = remember(selectedSubject, targetGrade, gradeScale, minPassingGrade) {
        calculateRequiredGrade(
            evaluations = selectedSubject.evaluations,
            targetGrade = targetGrade,
            scale = gradeScale,
            minPassingGrade = minPassingGrade
        )
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Subject Selector Chips
        if (subjectGrades.size > 1) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = stringResource(R.string.analytics_simulator_select_subject),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    subjectGrades.forEach { subjectInfo ->
                        val isSelected = subjectInfo.subject.equals(selectedSubjectName, ignoreCase = true)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                if (!isSelected) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    selectedSubjectName = subjectInfo.subject
                                }
                            },
                            label = {
                                Text(
                                    text = subjectInfo.subject,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.5f),
                                labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }
        }

        // Dedicated simulator card for the active subject
        GradeSimulatorCard(
            simulationResult = simulationResult,
            scale = gradeScale,
            targetGrade = targetGrade,
            onTargetGradeChange = { targetGrade = it }
        )
    }
}
