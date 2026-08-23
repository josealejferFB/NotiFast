package com.notifast.app.presentation.feature.grades.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.domain.model.SubjectAcademicHealth
import com.notifast.app.presentation.atoms.NotiFastCard
import com.notifast.app.presentation.theme.ThemeUtils
import com.notifast.app.presentation.theme.color.AquaGreen
import java.util.Locale

@Composable
fun AcademicProgressChart(
    subjects: List<SubjectAcademicHealth>,
    gradeScale: GradeScale,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(subjects) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(750, easing = FastOutSlowInEasing))
    }

    val outlineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f)
    val textMutedColor = MaterialTheme.colorScheme.onSurfaceVariant
    val passingLineColor = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
    val passingGrade = gradeScale.minPassingGrade
    val maxGrade = gradeScale.maxGrade

    NotiFastCard(
        modifier = modifier.fillMaxWidth(),
        onClick = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.analytics_chart_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.analytics_chart_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = textMutedColor
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            if (subjects.isEmpty()) {
                Text(
                    text = stringResource(R.string.analytics_no_data_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = textMutedColor,
                    modifier = Modifier.padding(vertical = 24.dp)
                )
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                ) {
                    drawChartContent(
                        subjects = subjects,
                        maxGrade = maxGrade,
                        passingGrade = passingGrade,
                        progress = animProgress.value,
                        isDark = isDark,
                        outlineColor = outlineColor,
                        textMutedColor = textMutedColor,
                        passingLineColor = passingLineColor
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawChartContent(
    subjects: List<SubjectAcademicHealth>,
    maxGrade: Double,
    passingGrade: Double,
    progress: Float,
    isDark: Boolean,
    outlineColor: Color,
    textMutedColor: Color,
    passingLineColor: Color
) {
    val paddingBottom = 28.dp.toPx()
    val paddingTop = 12.dp.toPx()
    val paddingLeft = 12.dp.toPx()
    val paddingRight = 12.dp.toPx()

    val chartWidth = size.width - paddingLeft - paddingRight
    val chartHeight = size.height - paddingBottom - paddingTop

    // Base horizontal axis
    val yBase = size.height - paddingBottom
    drawLine(
        color = outlineColor,
        start = Offset(paddingLeft, yBase),
        end = Offset(size.width - paddingRight, yBase),
        strokeWidth = 1.dp.toPx()
    )

    // Passing threshold dashed line
    val passingY = yBase - (passingGrade.toFloat() / maxGrade.toFloat() * chartHeight)
    val dashPathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
    drawLine(
        color = passingLineColor,
        start = Offset(paddingLeft, passingY),
        end = Offset(size.width - paddingRight, passingY),
        strokeWidth = 1.5.dp.toPx(),
        pathEffect = dashPathEffect
    )

    val itemCount = subjects.size
    val slotWidth = chartWidth / itemCount
    val barWidth = (slotWidth * 0.48f).coerceIn(12.dp.toPx(), 36.dp.toPx())

    subjects.forEachIndexed { index, subject ->
        val centerX = paddingLeft + (index * slotWidth) + (slotWidth / 2f)
        val subjectColor = ThemeUtils.getAccessibleColor(subject.colorHex ?: "", isDark)

        // Bar background (total max height track)
        val barLeft = centerX - (barWidth / 2f)
        drawRoundRect(
            color = outlineColor.copy(alpha = 0.15f),
            topLeft = Offset(barLeft, paddingTop),
            size = Size(barWidth, chartHeight),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )

        // Accumulated Points Fill Bar
        val pointsFraction = (subject.accumulatedPoints.toFloat() / maxGrade.toFloat()).coerceIn(0f, 1f)
        val barHeight = (pointsFraction * chartHeight * progress).coerceAtLeast(0f)
        val barTop = yBase - barHeight

        drawRoundRect(
            color = subjectColor,
            topLeft = Offset(barLeft, barTop),
            size = Size(barWidth, barHeight),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )

        // Subject Monogram Label
        val monogram = subject.subjectName.take(3).uppercase(Locale.ROOT)
        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                color = if (isDark) 0xFFE2E8F0.toInt() else 0xFF334155.toInt()
                textSize = 10.dp.toPx()
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
                typeface = android.graphics.Typeface.DEFAULT_BOLD
            }
            drawText(monogram, centerX, size.height - 6.dp.toPx(), paint)
        }
    }
}
