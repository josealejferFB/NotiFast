package com.notifast.app.presentation.feature.grades.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifast.app.R
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.domain.usecase.RequiredGradeResult
import com.notifast.app.domain.usecase.SimulatorStatus
import com.notifast.app.presentation.atoms.NotiFastCard
import com.notifast.app.presentation.theme.color.Aqua
import com.notifast.app.presentation.theme.color.Ruby
import com.notifast.app.presentation.theme.type.monoAcademicBold
import com.notifast.app.presentation.theme.type.monoAcademicLarge

@Composable
fun GradeSimulatorCard(
    simulationResult: RequiredGradeResult?,
    scale: GradeScale,
    targetGrade: Double,
    onTargetGradeChange: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    NotiFastCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = 20.dp
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Aqua.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Calculate,
                        contentDescription = null,
                        tint = Aqua,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = stringResource(R.string.simulator_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.simulator_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (simulationResult != null) {
                // Status Pill Banner
                val (statusColor, statusIcon, statusText) = when (simulationResult.status) {
                    SimulatorStatus.ALREADY_PASSED -> Triple(Aqua, Icons.Default.CheckCircle, stringResource(R.string.simulator_status_passed))
                    SimulatorStatus.ACHIEVABLE -> Triple(Aqua, Icons.Default.CheckCircle, stringResource(R.string.simulator_status_achievable))
                    SimulatorStatus.AT_RISK -> Triple(MaterialTheme.colorScheme.tertiary, Icons.Default.Warning, stringResource(R.string.simulator_status_at_risk))
                    SimulatorStatus.IMPOSSIBLE -> Triple(Ruby, Icons.Default.Warning, stringResource(R.string.simulator_status_impossible))
                    SimulatorStatus.NO_PENDING_EVALUATIONS -> Triple(MaterialTheme.colorScheme.onSurfaceVariant, Icons.Default.Info, stringResource(R.string.simulator_no_pending_weight))
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = statusColor.copy(alpha = 0.12f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = statusColor
                        )
                    }
                }

                // Stats row (Accumulated vs Required)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = stringResource(R.string.simulator_accumulated_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = scale.formatGrade(simulationResult.currentAccumulated),
                            style = MaterialTheme.typography.monoAcademicLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = stringResource(R.string.simulator_required_grade_label),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val dashText = stringResource(R.string.grades_average_dash)
                        Text(
                            text = if (simulationResult.requiredGrade != null) scale.formatGrade(simulationResult.requiredGrade) else dashText,
                            style = MaterialTheme.typography.monoAcademicLarge,
                            color = statusColor
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.simulator_remaining_weight, simulationResult.remainingWeight),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Slider for Target Grade
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(R.string.simulator_target_grade),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Ruby.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = scale.formatGrade(targetGrade),
                                style = MaterialTheme.typography.monoAcademicBold,
                                color = Ruby,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    val sliderRange = scale.minGrade.toFloat()..scale.maxGrade.toFloat()
                    Slider(
                        value = targetGrade.toFloat().coerceIn(sliderRange),
                        onValueChange = { onTargetGradeChange(it.toDouble()) },
                        valueRange = sliderRange,
                        steps = when (scale) {
                            GradeScale.SCALE_0_10 -> 19 // increments of 0.5
                            GradeScale.SCALE_0_20 -> 19 // increments of 1.0
                            GradeScale.SCALE_0_100 -> 99
                            GradeScale.SCALE_LETTER -> 7
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = Ruby,
                            activeTrackColor = Ruby,
                            inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
    }
}
