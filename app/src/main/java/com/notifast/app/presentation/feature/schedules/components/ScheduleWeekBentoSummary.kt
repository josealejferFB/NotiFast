package com.notifast.app.presentation.feature.schedules.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifast.app.R
import com.notifast.app.presentation.feature.schedules.WeeklyScheduleSummary
import com.notifast.app.presentation.theme.type.JetBrainsMonoFamily

/**
 * NotiFast 3.0 Weekly Bento Summary Dashboard.
 * Displays academic load metrics, weekly distribution bars, and subject time breakdown.
 */
@Composable
fun ScheduleWeekBentoSummary(
    summary: WeeklyScheduleSummary,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(bottom = 120.dp, top = 8.dp)
) {
    val isDark = isSystemInDarkTheme()
    val totalHours = String.format(java.util.Locale.US, "%.1f", summary.totalWeeklyMinutes / 60f)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = contentPadding
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BentoMetricCard(
                    title = stringResource(R.string.schedules_bento_total_hours_title),
                    value = stringResource(R.string.schedules_bento_total_hours_value, totalHours),
                    subtitle = stringResource(R.string.schedules_bento_busiest_day_title) + ": " + summary.busiestDayName,
                    modifier = Modifier.weight(1f)
                )
                BentoMetricCard(
                    title = stringResource(R.string.schedules_bento_total_classes_title),
                    value = stringResource(R.string.schedules_bento_total_classes_value, summary.totalClasses),
                    subtitle = "${summary.dayMetrics.count { it.classCount > 0 }} días lectivos",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            WeeklyDistributionCard(dayMetrics = summary.dayMetrics)
        }

        if (summary.subjectMetrics.isNotEmpty()) {
            item {
                SubjectBreakdownCard(subjectMetrics = summary.subjectMetrics, isDark = isDark)
            }
        }
    }
}

@Composable
private fun BentoMetricCard(
    title: String,
    value: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontFamily = JetBrainsMonoFamily,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}
