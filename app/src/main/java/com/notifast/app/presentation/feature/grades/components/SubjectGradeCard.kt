package com.notifast.app.presentation.feature.grades.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifast.app.R
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.presentation.atoms.NotiFastCard
import com.notifast.app.presentation.feature.grades.SubjectGradeInfo
import com.notifast.app.presentation.feature.notes.components.SubjectIconMapper
import com.notifast.app.presentation.theme.ThemeUtils
import com.notifast.app.presentation.theme.isDark
import com.notifast.app.presentation.theme.type.JetBrainsMonoFamily
import com.notifast.app.presentation.util.GradeFormatters

/**
 * NotiFast 3.0 Subject Grade Card.
 * Displays subject averages and expandable list of evaluations with spring animations and haptic feedback.
 */
@Composable
fun SubjectGradeCard(
    subjectGrade: SubjectGradeInfo,
    onUpdateGrade: (String, Double?, Boolean) -> Unit,
    displayScale: GradeScale,
    minPassingGrade: Double
) {
    val haptic = LocalHapticFeedback.current
    var expanded by remember { mutableStateOf(false) }
    val subject = subjectGrade.subject
    val evaluations = subjectGrade.evaluations
    val subjectAverage = subjectGrade.average
    val isDark = MaterialTheme.colorScheme.isDark
    val subjectColor = ThemeUtils.getAccessibleColor(subjectGrade.colorHex, isDark)

    NotiFastCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outlineVariant,
        contentPadding = 16.dp,
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            expanded = !expanded
        }
    ) {
        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        color = subjectColor.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = SubjectIconMapper.getIcon(subjectGrade.icon),
                            contentDescription = subject,
                            tint = subjectColor,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = subject,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        val countText = if (evaluations.size == 1) {
                            stringResource(R.string.grades_evaluations_count_single)
                        } else {
                            stringResource(R.string.grades_evaluations_count, evaluations.size)
                        }
                        Text(
                            text = countText,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    val hasGrades = subjectAverage != null
                    val isPassing = (subjectAverage ?: 0.0) >= minPassingGrade
                    val gradeColor = when {
                        !hasGrades -> MaterialTheme.colorScheme.onSurfaceVariant
                        isPassing -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.error
                    }
                    val gradeText = if (hasGrades) {
                        GradeFormatters.average(subjectAverage ?: 0.0, displayScale)
                    } else {
                        stringResource(R.string.grades_average_dash)
                    }
                    Text(
                        text = gradeText,
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 20.sp,
                        color = gradeColor
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
                    Spacer(modifier = Modifier.height(6.dp))
                    if (evaluations.isEmpty()) {
                        Text(
                            text = stringResource(R.string.grades_empty_no_evaluations_subject),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        evaluations.forEach { eval ->
                            EvaluationDetailRow(
                                evaluation = eval,
                                displayScale = displayScale,
                                minPassingGrade = minPassingGrade,
                                onUpdateGrade = onUpdateGrade
                            )
                        }
                    }
                }
            }
        }
    }
}
