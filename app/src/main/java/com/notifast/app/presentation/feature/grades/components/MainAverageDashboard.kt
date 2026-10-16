package com.notifast.app.presentation.feature.grades.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.unit.sp
import com.notifast.app.R
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.presentation.atoms.NotiFastCard
import com.notifast.app.presentation.theme.type.JetBrainsMonoFamily
import com.notifast.app.presentation.util.GradeFormatters

/**
 * NotiFast 3.0 Studio Notebook Global Average Hero Block.
 * Features JetBrains Mono tabular digits, tactile papercraft card,
 * and semantic status color indicators (Tertiary/ForestSage for passing, Error/Ruby for at risk).
 */
@Composable
fun MainAverageDashboard(
    generalAverage: Double?,
    gradeScale: GradeScale = GradeScale.SCALE_0_10,
    minPassingGrade: Double = gradeScale.minPassingGrade
) {
    val hasGrades = generalAverage != null
    val isPassing = (generalAverage ?: 0.0) >= minPassingGrade
    val statusColor = when {
        !hasGrades -> MaterialTheme.colorScheme.primary
        isPassing -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.error
    }

    NotiFastCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
        borderColor = MaterialTheme.colorScheme.outlineVariant,
        contentPadding = 16.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.grades_average_global),
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            val averageText = if (hasGrades) {
                GradeFormatters.average(generalAverage ?: 0.0, gradeScale)
            } else {
                stringResource(R.string.grades_average_dash)
            }

            Text(
                text = averageText,
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 54.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = JetBrainsMonoFamily,
                letterSpacing = (-1).sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            val maxScale = gradeScale.maxGrade.toFloat()
            val progress = if (hasGrades) {
                ((generalAverage ?: 0.0).toFloat() / maxScale).coerceIn(0f, 1f)
            } else {
                0f
            }

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = statusColor,
                trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )

            Spacer(modifier = Modifier.height(12.dp))

            val messageText = when {
                !hasGrades -> stringResource(R.string.grades_average_no_evaluations)
                isPassing -> stringResource(R.string.grades_average_excellent)
                else -> stringResource(R.string.grades_average_keep_going)
            }

            Text(
                text = messageText,
                color = if (hasGrades) statusColor else MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
