package com.notifast.app.presentation.feature.grades.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifast.app.R
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.domain.model.SubjectAcademicHealth
import com.notifast.app.domain.model.SubjectHealthStatus
import com.notifast.app.presentation.atoms.NotiFastCard
import com.notifast.app.presentation.feature.notes.components.SubjectIconMapper
import com.notifast.app.presentation.theme.ThemeUtils
import com.notifast.app.presentation.theme.color.TerracottaWarm
import com.notifast.app.presentation.theme.isDark
import com.notifast.app.presentation.theme.type.monoAcademicBold
import com.notifast.app.presentation.util.GradeFormatters

/**
 * Clean, readable list card showing academic health per subject.
 * Replaces abstract canvas charts with clear progress tracks and status indicators
 * tailored for high school and university students.
 */
@Composable
fun SubjectHealthListCard(
    subjects: List<SubjectAcademicHealth>,
    gradeScale: GradeScale,
    minPassingGrade: Double = gradeScale.minPassingGrade,
    modifier: Modifier = Modifier
) {
    val isDark = MaterialTheme.colorScheme.isDark

    NotiFastCard(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        contentPadding = 16.dp,
        onClick = null
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column {
                Text(
                    text = stringResource(R.string.analytics_subjects_status_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.analytics_subjects_status_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (subjects.isEmpty()) {
                Text(
                    text = stringResource(R.string.analytics_no_data_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                subjects.forEach { subject ->
                    SubjectHealthRow(
                        subject = subject,
                        gradeScale = gradeScale,
                        minPassingGrade = minPassingGrade,
                        isDark = isDark
                    )
                }
            }
        }
    }
}

@Composable
private fun SubjectHealthRow(
    subject: SubjectAcademicHealth,
    gradeScale: GradeScale,
    minPassingGrade: Double,
    isDark: Boolean
) {
    val subjectColor = ThemeUtils.getAccessibleColor(subject.colorHex ?: "", isDark)

    val (statusLabel, statusColor) = when (subject.status) {
        SubjectHealthStatus.APPROVED -> stringResource(R.string.analytics_status_approved) to MaterialTheme.colorScheme.tertiary
        SubjectHealthStatus.ON_TRACK -> {
            if (subject.evaluatedWeight > 0) {
                stringResource(R.string.analytics_status_on_track) to MaterialTheme.colorScheme.tertiary
            } else {
                stringResource(R.string.analytics_status_no_evals) to MaterialTheme.colorScheme.onSurfaceVariant
            }
        }
        SubjectHealthStatus.AT_RISK -> stringResource(R.string.analytics_status_at_risk) to TerracottaWarm
        SubjectHealthStatus.CRITICAL_FAIL -> stringResource(R.string.analytics_status_critical) to MaterialTheme.colorScheme.error
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .padding(vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    color = subjectColor.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = SubjectIconMapper.getIcon(subject.subjectName),
                        contentDescription = subject.subjectName,
                        tint = subjectColor,
                        modifier = Modifier.padding(7.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = subject.subjectName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1
                    )
                    val ptsStr = GradeFormatters.average(subject.accumulatedPoints, gradeScale)
                    val metaText = if (subject.remainingWeight > 0.0) {
                        stringResource(R.string.analytics_pts_and_weight, ptsStr, subject.remainingWeight)
                    } else {
                        stringResource(R.string.analytics_pts_no_weight, ptsStr)
                    }
                    Text(
                        text = metaText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(6.dp),
                color = statusColor.copy(alpha = 0.14f)
            ) {
                Text(
                    text = statusLabel,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.monoAcademicBold,
                    fontSize = 11.sp,
                    color = statusColor
                )
            }
        }

        // Progress towards passing grade
        val maxPoints = gradeScale.maxGrade.toFloat().coerceAtLeast(1f)
        val progressFraction = (subject.accumulatedPoints.toFloat() / maxPoints).coerceIn(0f, 1f)

        LinearProgressIndicator(
            progress = { progressFraction },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(CircleShape),
            color = statusColor,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    }
}
