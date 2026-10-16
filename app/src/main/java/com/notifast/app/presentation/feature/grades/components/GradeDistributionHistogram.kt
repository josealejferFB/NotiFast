package com.notifast.app.presentation.feature.grades.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.domain.model.GradeBandDistribution
import com.notifast.app.presentation.atoms.NotiFastCard
import com.notifast.app.presentation.theme.color.TerracottaWarm
import com.notifast.app.presentation.theme.type.monoAcademicBold

@Composable
fun GradeDistributionHistogram(
    distribution: GradeBandDistribution,
    modifier: Modifier = Modifier
) {
    val animProgress = remember { Animatable(0f) }

    LaunchedEffect(distribution) {
        animProgress.snapTo(0f)
        animProgress.animateTo(1f, animationSpec = tween(750, easing = FastOutSlowInEasing))
    }

    val total = distribution.totalEvaluated.coerceAtLeast(1)

    val bands = listOf(
        HistogramBandItem(
            label = stringResource(R.string.analytics_band_excellent),
            count = distribution.excellentCount,
            fraction = distribution.excellentCount.toFloat() / total,
            color = MaterialTheme.colorScheme.tertiary
        ),
        HistogramBandItem(
            label = stringResource(R.string.analytics_band_good),
            count = distribution.goodCount,
            fraction = distribution.goodCount.toFloat() / total,
            color = MaterialTheme.colorScheme.secondary
        ),
        HistogramBandItem(
            label = stringResource(R.string.analytics_band_regular),
            count = distribution.regularCount,
            fraction = distribution.regularCount.toFloat() / total,
            color = TerracottaWarm
        ),
        HistogramBandItem(
            label = stringResource(R.string.analytics_band_at_risk),
            count = distribution.atRiskCount,
            fraction = distribution.atRiskCount.toFloat() / total,
            color = MaterialTheme.colorScheme.error
        )
    )

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
            Column {
                Text(
                    text = stringResource(R.string.analytics_histogram_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.analytics_histogram_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(4.dp))

            bands.forEach { band ->
                BandRow(band = band, progress = animProgress.value)
            }
        }
    }
}

private data class HistogramBandItem(
    val label: String,
    val count: Int,
    val fraction: Float,
    val color: Color
)

@Composable
private fun BandRow(band: HistogramBandItem, progress: Float) {
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val percentage = (band.fraction * 100).toInt()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = band.label,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.width(92.dp)
        )

        Canvas(
            modifier = Modifier
                .weight(1f)
                .height(10.dp)
                .padding(horizontal = 8.dp)
        ) {
            // Background track using surfaceContainerHigh
            drawRoundRect(
                color = trackColor,
                topLeft = Offset.Zero,
                size = size,
                cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
            )

            // Animated fill bar
            val fillWidth = size.width * band.fraction * progress
            if (fillWidth > 0f) {
                drawRoundRect(
                    color = band.color,
                    topLeft = Offset.Zero,
                    size = Size(fillWidth, size.height),
                    cornerRadius = CornerRadius(5.dp.toPx(), 5.dp.toPx())
                )
            }
        }

        Text(
            text = stringResource(R.string.analytics_histogram_band_format, band.count, percentage),
            style = MaterialTheme.typography.monoAcademicBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(64.dp)
        )
    }
}
