package com.notifast.app.presentation.atoms

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notifast.app.R

/**
 * Banner de alerta táctil AAA para NotiFast 3.0.
 *
 * Utilizado para mensajes de error de pantalla, validaciones mayores en formularios
 * y avisos del sistema. Reemplaza los Toast y mensajes crudos de Android con una
 * experiencia integrada, animada y accesible.
 */
@Composable
fun NotiFastAlertBanner(
    message: String,
    modifier: Modifier = Modifier,
    title: String? = null,
    severity: AlertSeverity = AlertSeverity.ERROR,
    icon: ImageVector? = severity.defaultIcon,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    onDismiss: (() -> Unit)? = null,
    visible: Boolean = true
) {
    val haptic = LocalHapticFeedback.current
    val colors = severity.colors()

    val severityPrefix = when (severity) {
        AlertSeverity.ERROR -> stringResource(R.string.alert_severity_error_prefix, message)
        AlertSeverity.WARNING -> stringResource(R.string.alert_severity_warning_prefix, message)
        AlertSeverity.SUCCESS -> stringResource(R.string.alert_severity_success_prefix, message)
        AlertSeverity.INFO -> stringResource(R.string.alert_severity_info_prefix, message)
    }

    AnimatedVisibility(
        visible = visible && message.isNotBlank(),
        enter = expandVertically(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMediumLow)) +
                fadeIn(spring(stiffness = Spring.StiffnessMediumLow)),
        exit = shrinkVertically(spring(stiffness = Spring.StiffnessHigh)) +
                fadeOut(spring(stiffness = Spring.StiffnessHigh)),
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .semantics {
                    liveRegion = LiveRegionMode.Polite
                    contentDescription = severityPrefix
                },
            shape = RoundedCornerShape(16.dp),
            color = colors.containerColor,
            border = BorderStroke(1.dp, colors.borderColor),
            contentColor = colors.contentColor
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Insignia circular del icono
                if (icon != null) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(colors.iconBadgeContainerColor, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = colors.contentColor,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                }

                // Columna de título y mensaje
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    if (!title.isNullOrBlank()) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = colors.contentColor
                        )
                    }
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.contentColor
                    )

                    if (!actionLabel.isNullOrBlank() && onAction != null) {
                        TextButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onAction()
                            },
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Text(
                                text = actionLabel,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = colors.contentColor
                            )
                        }
                    }
                }

                // Botón de descarte opcional
                if (onDismiss != null) {
                    IconButton(
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onDismiss()
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.alert_dismiss_cd),
                            modifier = Modifier.size(18.dp),
                            tint = colors.contentColor
                        )
                    }
                }
            }
        }
    }
}
