package com.notifast.app.presentation.feature.cloud.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifast.app.R
import com.notifast.app.domain.model.cloud.BatchResult
import com.notifast.app.presentation.atoms.NotiFastButton
import com.notifast.app.presentation.theme.type.monoAcademicBold

/**
 * NotiFast 3.0 Cloud Sync Summary Card AAA.
 * Displays sync outcome metrics with tabular monospace typography,
 * spring micro-interactions and tactile haptic response.
 */
@Composable
fun CloudSummaryCard(
    summary: BatchResult,
    onRetry: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val total = summary.outcomes.size
    val failed = summary.failed.size
    val noOp = summary.noOpCount
    val succeeded = summary.actualSuccessCount

    val borderColor = if (failed > 0) {
        MaterialTheme.colorScheme.error.copy(alpha = 0.40f)
    } else {
        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.40f)
    }

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(
                            if (failed > 0) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.45f)
                            else MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (failed > 0) Icons.Rounded.ErrorOutline else Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = if (failed > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = stringResource(R.string.cloud_summary_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (succeeded > 0) {
                SummaryMetricRow(
                    icon = Icons.Rounded.CheckCircle,
                    color = MaterialTheme.colorScheme.tertiary,
                    text = stringResource(R.string.cloud_summary_success_count, succeeded)
                )
            }

            if (noOp > 0) {
                SummaryMetricRow(
                    icon = Icons.Rounded.Info,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    text = stringResource(R.string.cloud_summary_noop_count, noOp)
                )
            }

            if (failed > 0) {
                SummaryMetricRow(
                    icon = Icons.Rounded.ErrorOutline,
                    color = MaterialTheme.colorScheme.error,
                    text = stringResource(R.string.cloud_summary_failure_count, failed)
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onDismiss()
                    }
                ) {
                    Text(stringResource(R.string.btn_close))
                }

                if (failed > 0) {
                    NotiFastButton(
                        text = stringResource(R.string.cloud_summary_btn_retry),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onRetry()
                        },
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryMetricRow(
    icon: ImageVector,
    color: Color,
    text: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.monoAcademicBold.copy(fontSize = 13.sp),
            color = color
        )
    }
}
