package com.notifast.app.presentation.feature.grades.components

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
import com.notifast.app.presentation.feature.grades.SubjectGradeInfo
import com.notifast.app.presentation.theme.type.monoAcademicLarge

/**
 * Predictive and diagnostic academic analytics view for NotiFast 3.0 Grades Hub.
 * Streamlined specifically for secondary and university students:
 * 1. Academic Pulse & Projections Hero
 * 2. Real-time Interactive Simulator
 * 3. Subject Health Progress List
 * 4. Early Warning Diagnostic Alerts
 * 5. Tactical Recommendations
 */
@Composable
fun AcademicAnalyticsContent(
    stats: AcademicStats,
    gradeScale: GradeScale,
    minPassingGrade: Double = gradeScale.minPassingGrade,
    subjectGrades: List<SubjectGradeInfo> = emptyList(),
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Hero Pulse Card
        AnalyticsHeroCard(stats = stats)

        // 2. Interactive Simulator (The most requested tool for students)
        if (subjectGrades.isNotEmpty()) {
            IntegratedGradeSimulatorCard(
                subjectGrades = subjectGrades,
                gradeScale = gradeScale,
                minPassingGrade = minPassingGrade
            )
        }

        // 3. Clear Subject Health Progress List
        SubjectHealthListCard(
            subjects = stats.subjectHealthList,
            gradeScale = gradeScale,
            minPassingGrade = minPassingGrade
        )

        // 4. Early Warning Alerts Card (Shows alerts when subjects need attention)
        if (stats.atRiskSubjects.isNotEmpty()) {
            AcademicEarlyWarningCard(
                atRiskSubjects = stats.atRiskSubjects,
                displayScale = gradeScale
            )
        }

        // 5. Tactical Recommendations (Actionable guidance)
        if (stats.studyRecommendations.isNotEmpty()) {
            RecommendationsCard(recommendations = stats.studyRecommendations)
        }
    }
}

@Composable
private fun AnalyticsHeroCard(stats: AcademicStats) {
    val ratePercentage = stats.projectedPassingRate.toInt()
    val allGood = stats.atRiskSubjects.isEmpty()
    val accentColor = if (allGood) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error

    val headerText = if (allGood) {
        stringResource(R.string.analytics_pulse_all_good)
    } else {
        stringResource(R.string.analytics_projected_rate_label)
    }

    val descText = when {
        stats.totalSubjectsCount == 0 -> {
            stringResource(R.string.analytics_no_data_desc)
        }
        allGood -> {
            stringResource(R.string.analytics_pulse_all_good_desc)
        }
        else -> {
            stringResource(R.string.analytics_pulse_attention_desc, stats.atRiskSubjects.size)
        }
    }

    NotiFastCard(
        modifier = Modifier.fillMaxWidth(),
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = headerText,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = descText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = "$ratePercentage%",
                    style = MaterialTheme.typography.monoAcademicLarge,
                    fontWeight = FontWeight.Bold,
                    color = accentColor
                )
            }

            val progressValue = (stats.projectedPassingRate / 100.0).toFloat().coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { progressValue },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(6.dp)),
                color = accentColor,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        }
    }
}

@Composable
private fun RecommendationsCard(recommendations: List<String>) {
    NotiFastCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        contentPadding = 16.dp,
        onClick = null
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
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
                        text = stringResource(R.string.analytics_recommendation_item, recommendation),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}
