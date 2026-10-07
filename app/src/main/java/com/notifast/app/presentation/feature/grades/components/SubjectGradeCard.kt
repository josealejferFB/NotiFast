package com.notifast.app.presentation.feature.grades.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Pending
import androidx.compose.material.icons.rounded.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.res.stringResource
import com.notifast.app.R
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifast.app.data.model.Evaluation
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.presentation.feature.grades.SubjectGradeInfo
import com.notifast.app.presentation.theme.type.JetBrainsMonoFamily
import com.notifast.app.presentation.util.GradeFormatters

/**
 * NotiFast 3.0 Subject Grade Card.
 * Displays subject averages and expandable list of evaluations.
 */
@Composable
fun SubjectGradeCard(
    subjectGrade: SubjectGradeInfo,
    onUpdateGrade: (String, Double?, Boolean) -> Unit,
    displayScale: GradeScale,
    minPassingGrade: Double
) {
    var expanded by remember { mutableStateOf(false) }
    val subject = subjectGrade.subject
    val evaluations = subjectGrade.evaluations
    val subjectAverage = subjectGrade.average

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.School,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = subject,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            maxLines = 1,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${evaluations.size} evaluaciones",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    val isPassing = subjectAverage >= minPassingGrade
                    Text(
                        text = GradeFormatters.average(subjectAverage, displayScale),
                        fontWeight = FontWeight.Bold,
                        fontFamily = JetBrainsMonoFamily,
                        fontSize = 20.sp,
                        color = if (isPassing) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                }
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(modifier = Modifier.height(8.dp))
                    evaluations.forEach { eval ->
                        EvaluationDetailRow(
                            evaluation = eval,
                            displayScale = displayScale,
                            onUpdateGrade = onUpdateGrade
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EvaluationDetailRow(
    evaluation: Evaluation,
    displayScale: GradeScale,
    onUpdateGrade: (String, Double?, Boolean) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { showDialog = true },
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
                Icon(
                    imageVector = if (evaluation.isPresented) Icons.Rounded.CheckCircle else Icons.Rounded.Pending,
                    contentDescription = null,
                    tint = if (evaluation.isPresented) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(
                        if (evaluation.isPresented) R.string.grades_status_evaluated else R.string.grades_status_pending
                    ),
                    fontSize = 11.sp,
                    color = if (evaluation.isPresented) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Surface(
            color = if (evaluation.grade != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(10.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            val gradeStr = evaluation.grade?.let {
                GradeFormatters.grade(it, evaluation.gradeScaleId, displayScale)
            } ?: "—"
            Text(
                text = gradeStr,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                fontWeight = FontWeight.Bold,
                fontFamily = JetBrainsMonoFamily,
                fontSize = 13.sp,
                color = if (evaluation.grade != null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
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
