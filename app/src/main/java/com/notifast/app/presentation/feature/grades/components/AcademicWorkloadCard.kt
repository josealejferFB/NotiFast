package com.notifast.app.presentation.feature.grades.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.domain.model.SubjectAcademicHealth
import com.notifast.app.presentation.atoms.NotiFastCard
import com.notifast.app.presentation.theme.type.monoAcademicBold
import com.notifast.app.presentation.util.GradeFormatters

@Composable
fun AcademicWorkloadCard(
    totalWeeklyHours: Double,
    bestSubject: SubjectAcademicHealth?,
    lowestSubject: SubjectAcademicHealth?,
    displayScale: GradeScale = GradeScale.SCALE_0_10,
    modifier: Modifier = Modifier
) {
    NotiFastCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        contentPadding = 16.dp,
        onClick = null
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.analytics_workload_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.analytics_workload_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = stringResource(R.string.analytics_workload_total_hours, totalWeeklyHours),
                        style = MaterialTheme.typography.monoAcademicBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            if (bestSubject != null || lowestSubject != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    bestSubject?.let { best ->
                        WorkloadPill(
                            label = stringResource(R.string.analytics_best_subject_label),
                            subjectName = best.subjectName,
                            average = best.currentAverage,
                            displayScale = displayScale,
                            color = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    lowestSubject?.let { lowest ->
                        WorkloadPill(
                            label = stringResource(R.string.analytics_lowest_subject_label),
                            subjectName = lowest.subjectName,
                            average = lowest.currentAverage,
                            displayScale = displayScale,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun WorkloadPill(
    label: String,
    subjectName: String,
    average: Double,
    displayScale: GradeScale,
    color: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(10.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = subjectName,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1
        )
        Text(
            text = stringResource(
                R.string.analytics_workload_prom_avg,
                GradeFormatters.average(average, displayScale)
            ),
            style = MaterialTheme.typography.monoAcademicBold,
            color = color
        )
    }
}
