package com.notifast.app.presentation.feature.subjectdetail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifast.app.R
import com.notifast.app.data.model.Evaluation
import com.notifast.app.presentation.atoms.NotiFastButton
import com.notifast.app.presentation.atoms.NotiFastCard
import com.notifast.app.presentation.atoms.NotiFastEmptyState
import com.notifast.app.presentation.theme.type.monoAcademicBold

@Composable
fun SubjectEvaluationsTab(
    evaluations: List<Evaluation>,
    assignedWeight: Double,
    gradedWeight: Double,
    availableWeight: Double,
    onAddEvaluationClick: () -> Unit,
    onEditGradeClick: (Evaluation) -> Unit,
    onDeleteEvaluationClick: (Evaluation) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Weight Totalizer Card
        WeightTotalizerCard(
            assignedWeight = assignedWeight,
            gradedWeight = gradedWeight,
            availableWeight = availableWeight
        )

        if (evaluations.isEmpty()) {
            NotiFastEmptyState(
                title = stringResource(R.string.subject_detail_eval_empty_title),
                message = stringResource(R.string.subject_detail_eval_empty_desc),
                actionLabel = stringResource(R.string.subject_detail_eval_add_btn),
                onActionClick = onAddEvaluationClick
            )
        } else {
            evaluations.forEach { evaluation ->
                SubjectEvaluationItemCard(
                    evaluation = evaluation,
                    onEditGradeClick = onEditGradeClick,
                    onDeleteClick = onDeleteEvaluationClick
                )
            }
        }
    }
}

@Composable
private fun WeightTotalizerCard(
    assignedWeight: Double,
    gradedWeight: Double,
    availableWeight: Double
) {
    NotiFastCard(modifier = Modifier.fillMaxWidth(), contentPadding = 16.dp) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.subject_detail_weight_totalizer_title),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WeightMetricBadge(
                    label = stringResource(R.string.subject_detail_weight_graded, gradedWeight),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    textColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )
                WeightMetricBadge(
                    label = stringResource(R.string.subject_detail_weight_assigned, assignedWeight),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    textColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f)
                )
                WeightMetricBadge(
                    label = stringResource(R.string.subject_detail_weight_available, availableWeight),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun WeightMetricBadge(
    label: String,
    color: androidx.compose.ui.graphics.Color,
    textColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = color,
        modifier = modifier
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.monoAcademicBold,
            color = textColor,
            fontSize = 11.sp,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 6.dp)
        )
    }
}
