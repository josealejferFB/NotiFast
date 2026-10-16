package com.notifast.app.presentation.atoms

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.notifast.app.R

/**
 * Chip de alerta profesional AAA para NotiFast 3.0.
 *
 * Diseñado para validaciones en línea, badges de advertencia/error y tags de estado críticos.
 * Cuenta con física táctil háptica, soporte de accesibilidad TalkBack con liveRegion,
 * micro-botón de descarte opcional y colores de contraste alto.
 */
@Composable
fun NotiFastAlertChip(
    text: String,
    modifier: Modifier = Modifier,
    severity: AlertSeverity = AlertSeverity.ERROR,
    icon: ImageVector? = severity.defaultIcon,
    visible: Boolean = true,
    onClick: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null
) {
    val haptic = LocalHapticFeedback.current
    val colors = severity.colors()
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.96f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "alert_chip_scale"
    )

    val severityPrefix = when (severity) {
        AlertSeverity.ERROR -> stringResource(R.string.alert_severity_error_prefix, text)
        AlertSeverity.WARNING -> stringResource(R.string.alert_severity_warning_prefix, text)
        AlertSeverity.SUCCESS -> stringResource(R.string.alert_severity_success_prefix, text)
        AlertSeverity.INFO -> stringResource(R.string.alert_severity_info_prefix, text)
    }

    AnimatedVisibility(
        visible = visible && text.isNotBlank(),
        enter = fadeIn(spring(stiffness = Spring.StiffnessHigh)) + scaleIn(initialScale = 0.85f),
        exit = fadeOut(spring(stiffness = Spring.StiffnessHigh)) + scaleOut(targetScale = 0.85f)
    ) {
        Surface(
            modifier = modifier
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .semantics {
                    liveRegion = LiveRegionMode.Polite
                    contentDescription = severityPrefix
                }
                .then(
                    if (onClick != null) {
                        Modifier.clickable(
                            interactionSource = interactionSource,
                            indication = null,
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                onClick()
                            }
                        )
                    } else Modifier
                ),
            shape = CircleShape,
            color = colors.containerColor,
            border = BorderStroke(1.dp, colors.borderColor),
            contentColor = colors.contentColor
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp),
                        tint = colors.contentColor
                    )
                }

                Text(
                    text = text,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                if (onDismiss != null) {
                    Spacer(modifier = Modifier.width(2.dp))
                    Surface(
                        modifier = Modifier
                            .size(16.dp)
                            .clickable(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onDismiss()
                                }
                            ),
                        shape = CircleShape,
                        color = colors.contentColor.copy(alpha = 0.15f)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.alert_dismiss_cd),
                            modifier = Modifier.padding(2.dp),
                            tint = colors.contentColor
                        )
                    }
                }
            }
        }
    }
}
