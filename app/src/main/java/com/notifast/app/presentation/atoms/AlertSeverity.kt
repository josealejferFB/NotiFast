package com.notifast.app.presentation.atoms

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.notifast.app.presentation.theme.color.AquaGreen
import com.notifast.app.presentation.theme.color.ForestSage
import com.notifast.app.presentation.theme.color.MutedCrimson

/**
 * Severidad semántica para componentes de alerta AAA en NotiFast 3.0.
 */
enum class AlertSeverity {
    ERROR,
    WARNING,
    SUCCESS,
    INFO;

    val defaultIcon: ImageVector
        get() = when (this) {
            ERROR -> Icons.Rounded.ErrorOutline
            WARNING -> Icons.Rounded.WarningAmber
            SUCCESS -> Icons.Rounded.CheckCircle
            INFO -> Icons.Rounded.Info
        }
}

/**
 * Resuelve los colores semánticos de contenedor, contenido y borde según la severidad y el tema activo.
 */
data class AlertColors(
    val containerColor: Color,
    val contentColor: Color,
    val borderColor: Color,
    val iconBadgeContainerColor: Color
)

@Composable
@ReadOnlyComposable
fun AlertSeverity.colors(): AlertColors {
    val colorScheme = MaterialTheme.colorScheme
    return when (this) {
        AlertSeverity.ERROR -> AlertColors(
            containerColor = colorScheme.errorContainer.copy(alpha = 0.55f),
            contentColor = colorScheme.error,
            borderColor = colorScheme.error.copy(alpha = 0.35f),
            iconBadgeContainerColor = colorScheme.error.copy(alpha = 0.12f)
        )
        AlertSeverity.WARNING -> {
            val warningColor = Color(0xFFD97706)
            AlertColors(
                containerColor = Color(0xFFFEF3C7).copy(alpha = if (isThemeDark(colorScheme)) 0.20f else 0.85f),
                contentColor = if (isThemeDark(colorScheme)) Color(0xFFFBBF24) else warningColor,
                borderColor = if (isThemeDark(colorScheme)) Color(0xFFFBBF24).copy(alpha = 0.4f) else warningColor.copy(alpha = 0.35f),
                iconBadgeContainerColor = if (isThemeDark(colorScheme)) Color(0xFFFBBF24).copy(alpha = 0.15f) else warningColor.copy(alpha = 0.12f)
            )
        }
        AlertSeverity.SUCCESS -> AlertColors(
            containerColor = colorScheme.tertiaryContainer.copy(alpha = 0.55f),
            contentColor = if (isThemeDark(colorScheme)) colorScheme.tertiary else ForestSage,
            borderColor = (if (isThemeDark(colorScheme)) colorScheme.tertiary else ForestSage).copy(alpha = 0.35f),
            iconBadgeContainerColor = colorScheme.tertiary.copy(alpha = 0.12f)
        )
        AlertSeverity.INFO -> AlertColors(
            containerColor = colorScheme.primaryContainer.copy(alpha = 0.45f),
            contentColor = colorScheme.primary,
            borderColor = colorScheme.primary.copy(alpha = 0.30f),
            iconBadgeContainerColor = colorScheme.primary.copy(alpha = 0.12f)
        )
    }
}

private fun isThemeDark(colorScheme: ColorScheme): Boolean {
    // Estimación rápida de luminancia del fondo para contraste de temas
    val luminance = 0.299 * colorScheme.background.red + 0.587 * colorScheme.background.green + 0.114 * colorScheme.background.blue
    return luminance < 0.5
}
