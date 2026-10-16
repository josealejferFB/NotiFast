package com.notifast.app.presentation.feature.grades.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Pending
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifast.app.R
import com.notifast.app.data.model.Evaluation
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.presentation.theme.type.monoAcademicBold
import com.notifast.app.presentation.util.GradeFormatters

/**
 * Micro-card row representing an individual evaluation in NotiFast 3.0.
 * Includes tactile haptic feedback, semantic status iconography, and a tabular monospace grade chip.
 */
@Composable
fun EvaluationDetailRow(
    evaluation: Evaluation,
    displayScale: GradeScale,
    minPassingGrade: Double = displayScale.minPassingGrade,
    onUpdateGrade: (String, Double?, Boolean) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var showDialog by remember { mutableStateOf(false) }

    val isPassing = (evaluation.grade ?: 0.0) >= minPassingGrade
    val (chipBg, chipText) = when {
        evaluation.grade == null -> {
            MaterialTheme.colorScheme.surfaceContainerHigh to MaterialTheme.colorScheme.onSurfaceVariant
        }
        isPassing -> {
            MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.7f) to MaterialTheme.colorScheme.onTertiaryContainer
        }
        else -> {
            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f) to MaterialTheme.colorScheme.onErrorContainer
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                showDialog = true
            }
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = evaluation.title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                val isPresented = evaluation.isPresented
                val statusColor = if (isPresented) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                }
                Icon(
                    imageVector = if (isPresented) Icons.Rounded.CheckCircle else Icons.Rounded.Pending,
                    contentDescription = null,
                    tint = statusColor,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(
                        if (isPresented) R.string.grades_status_evaluated else R.string.grades_status_pending
                    ),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = statusColor
                )
            }
        }

        Surface(
            color = chipBg,
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
        ) {
            val dashText = stringResource(R.string.grades_average_dash)
            val gradeStr = evaluation.grade?.let {
                GradeFormatters.grade(it, evaluation.gradeScaleId, displayScale)
            } ?: dashText

            Text(
                text = gradeStr,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                style = MaterialTheme.typography.monoAcademicBold,
                fontSize = 13.sp,
                color = chipText
            )
        }
    }

    if (showDialog) {
        GradeRevampDialog(
            evaluation = evaluation,
            displayScale = displayScale,
            onDismiss = { showDialog = false },
            onSave = onUpdateGrade
        )
    }
}
