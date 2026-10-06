package com.notifast.app.presentation.feature.grades.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
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
import com.notifast.app.domain.model.AcademicStats
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.presentation.atoms.NotiFastCard
import com.notifast.app.presentation.theme.color.AquaGreen
import java.util.Locale

@Composable
fun AcademicAnalyticsContent(
    stats: AcademicStats,
    gradeScale: GradeScale,
    minPassingGrade: Double = gradeScale.minPassingGrade,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Diagnostic Card
        AnalyticsHeroCard(stats = stats)

        // Progress by Subject Chart
        AcademicProgressChart(
            subjects = stats.subjectHealthList,
            gradeScale = gradeScale,
            minPassingGrade = minPassingGrade
        )

        // Grade Distribution Histogram
        GradeDistributionHistogram(
            distribution = stats.gradeDistribution
        )

        // Early Warning Alerts Card
        AcademicEarlyWarningCard(
            atRiskSubjects = stats.atRiskSubjects,
            displayScale = gradeScale
        )

        // Workload Correlation Card
        AcademicWorkloadCard(
            totalWeeklyHours = stats.totalWeeklyClassHours,
            bestSubject = stats.bestSubject,
            lowestSubject = stats.lowestSubject,
            displayScale = gradeScale
        )

        // Recommendations
        if (stats.studyRecommendations.isNotEmpty()) {
            RecommendationsCard(recommendations = stats.studyRecommendations)
        }
    }
}

@Composable
private fun AnalyticsHeroCard(stats: AcademicStats) {
    val ratePercentage = stats.projectedPassingRate.toInt()
    val isGood = stats.projectedPassingRate >= 70.0
    val accentColor = if (isGood) AquaGreen else MaterialTheme.colorScheme.primary

    NotiFastCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.analytics_projected_rate_label),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(
                            R.string.analytics_projected_rate_desc,
                            stats.approvedSubjectsCount,
                            stats.totalSubjectsCount
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "$ratePercentage%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = accentColor
                )
            }

            LinearProgressIndicator(
                progress = { (stats.projectedPassingRate / 100.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp)),
                color = accentColor,
                trackColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
            )
        }
    }
}

@Composable
private fun RecommendationsCard(recommendations: List<String>) {
    NotiFastCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.analytics_recommendations_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            recommendations.forEach { recommendation ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(10.dp)
                ) {
                    Text(
                        text = "💡 $recommendation",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
