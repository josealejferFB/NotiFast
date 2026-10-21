package com.notifast.app.presentation.feature.subjectdetail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
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
import com.notifast.app.data.model.Schedule
import com.notifast.app.domain.util.ScheduleCalculations
import com.notifast.app.presentation.atoms.NotiFastCard
import com.notifast.app.presentation.theme.type.monoAcademicBold

/**
 * NotiFast 3.2 Academic Load Bento Summary for Subject Detail Schedules.
 * Displays total weekly hours, sessions count, modality distribution, and quick-add CTA.
 */
@Composable
fun SubjectSchedulesSummaryCard(
    schedules: List<Schedule>,
    onAddScheduleClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalMinutes = schedules.sumOf { schedule ->
        val s = ScheduleCalculations.timeToMinutes(schedule.startTime)
        val e = ScheduleCalculations.timeToMinutes(schedule.endTime)
        (e - s).coerceAtLeast(0)
    }
    val totalHoursStr = String.format(java.util.Locale.US, "%.1f", totalMinutes / 60f)

    val virtualCount = schedules.count { it.modality == Schedule.MODALITY_VIRTUAL }
    val presencialCount = schedules.count { it.modality != Schedule.MODALITY_VIRTUAL }
    val modalitySummary = when {
        virtualCount == schedules.size -> stringResource(R.string.schedules_modality_virtual)
        presencialCount == schedules.size -> stringResource(R.string.schedules_modality_presencial)
        else -> stringResource(R.string.schedules_modality_presencial) + " / " + stringResource(R.string.schedules_modality_virtual)
    }

    NotiFastCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = 16.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = stringResource(R.string.schedules_bento_total_classes_title),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                FilledTonalButton(
                    onClick = onAddScheduleClick,
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.subject_detail_action_schedule),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScheduleSummaryMetricPill(
                    label = stringResource(R.string.schedules_bento_total_hours_title),
                    value = stringResource(R.string.schedules_bento_total_hours_value, totalHoursStr),
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f)
                )
                ScheduleSummaryMetricPill(
                    label = stringResource(R.string.schedules_bento_total_classes_title),
                    value = stringResource(R.string.schedules_bento_total_classes_value, schedules.size),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.weight(1f)
                )
                ScheduleSummaryMetricPill(
                    label = stringResource(R.string.subject_detail_stat_status),
                    value = modalitySummary,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1.1f)
                )
            }
        }
    }
}

@Composable
private fun ScheduleSummaryMetricPill(
    label: String,
    value: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                fontSize = 11.sp,
                color = contentColor.copy(alpha = 0.8f)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.monoAcademicBold,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = contentColor,
                maxLines = 1
            )
        }
    }
}
