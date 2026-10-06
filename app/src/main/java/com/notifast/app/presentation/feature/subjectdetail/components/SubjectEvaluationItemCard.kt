package com.notifast.app.presentation.feature.subjectdetail.components

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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifast.app.R
import com.notifast.app.data.model.Evaluation
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.presentation.atoms.NotiFastCard
import com.notifast.app.presentation.theme.type.monoAcademicBold
import com.notifast.app.presentation.util.GradeFormatters
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun SubjectEvaluationItemCard(
    evaluation: Evaluation,
    displayScale: GradeScale = GradeScale.SCALE_0_10,
    onEditGradeClick: (Evaluation) -> Unit,
    onDeleteClick: (Evaluation) -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormat = SimpleDateFormat("dd MMM, yyyy", Locale.forLanguageTag("es-ES"))
    val isGraded = evaluation.isPresented && evaluation.grade != null

    NotiFastCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = 14.dp
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isGraded) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = evaluation.type,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isGraded) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    evaluation.weight?.let { weight ->
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "${weight.toInt()}%",
                            style = MaterialTheme.typography.monoAcademicBold,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = evaluation.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                evaluation.date?.let { date ->
                    Text(
                        text = dateFormat.format(date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Grade / Edit Pill
            Surface(
                onClick = { onEditGradeClick(evaluation) },
                shape = RoundedCornerShape(12.dp),
                color = if (isGraded) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.secondaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isGraded) {
                            GradeFormatters.grade(evaluation.grade, evaluation.gradeScaleId, displayScale)
                        } else {
                            stringResource(R.string.subject_detail_edit_grade_btn)
                        },
                        style = MaterialTheme.typography.monoAcademicBold,
                        color = if (isGraded) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                        fontSize = if (isGraded) 16.sp else 13.sp
                    )
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = if (isGraded) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            IconButton(
                onClick = { onDeleteClick(evaluation) },
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = stringResource(R.string.btn_delete),
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
