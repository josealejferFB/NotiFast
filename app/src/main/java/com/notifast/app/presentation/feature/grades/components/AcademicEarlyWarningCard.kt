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
import com.notifast.app.domain.model.SubjectHealthStatus
import com.notifast.app.presentation.atoms.NotiFastCard
import com.notifast.app.presentation.theme.color.TerracottaWarm
import com.notifast.app.presentation.theme.type.monoAcademicBold
import com.notifast.app.presentation.util.GradeFormatters
import java.util.Locale

@Composable
fun AcademicEarlyWarningCard(
    atRiskSubjects: List<SubjectAcademicHealth>,
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
                Text(
                    text = stringResource(R.string.analytics_early_warning_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                
                val badgeColor = if (atRiskSubjects.isEmpty()) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.error
                }
                val badgeText = if (atRiskSubjects.isEmpty()) "0" else "${atRiskSubjects.size}"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.monoAcademicBold,
                        color = badgeColor
                    )
                }
            }

            if (atRiskSubjects.isEmpty()) {
                Text(
                    text = stringResource(R.string.analytics_early_warning_all_good),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                atRiskSubjects.forEach { subject ->
                    EarlyWarningSubjectRow(
                        subject = subject,
                        displayScale = displayScale
                    )
                }
            }
        }
    }
}

@Composable
private fun EarlyWarningSubjectRow(
    subject: SubjectAcademicHealth,
    displayScale: GradeScale
) {
    val isCritical = subject.status == SubjectHealthStatus.CRITICAL_FAIL
    val tagColor = if (isCritical) MaterialTheme.colorScheme.error else TerracottaWarm
    val tagBg = tagColor.copy(alpha = 0.12f)
    val tagLabel = if (isCritical) {
        stringResource(R.string.analytics_early_warning_critical)
    } else {
        stringResource(R.string.analytics_early_warning_at_risk)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = subject.subjectName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            val neededStr = GradeFormatters.average(subject.minGradeNeededToPass, displayScale)
            val remStr = String.format(Locale.US, "%.0f", subject.remainingWeight)
            Text(
                text = stringResource(R.string.analytics_early_warning_pts_needed, neededStr, remStr),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(tagBg)
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = tagLabel,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = tagColor
            )
        }
    }
}
