package com.notifast.app.presentation.atoms

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.SnackbarData
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * CompositionLocal que provee la instancia central de [SnackbarHostState] para toda la aplicación.
 */
val LocalNotiFastSnackbarHostState = compositionLocalOf { SnackbarHostState() }

/**
 * Visuales enriquecidos para [NotiFastSnackbarHost] que permiten controlar la severidad
 * y título de la alerta en el sistema de diseño AAA.
 */
class NotiFastSnackbarVisuals(
    override val message: String,
    override val actionLabel: String? = null,
    override val withDismissAction: Boolean = true,
    override val duration: SnackbarDuration = SnackbarDuration.Short,
    val severity: AlertSeverity = AlertSeverity.SUCCESS,
    val title: String? = null
) : SnackbarVisuals

/**
 * Host de Notificaciones Emergentes y Snacks AAA para NotiFast 3.0.
 *
 * Reemplaza la alerta rectangular negra/oscura predeterminada de Android con un
 * [NotiFastAlertBanner] animado, respetando estrictamente los insets del sistema
 * (navigationBarsPadding) para evitar que quede oculta o superpuesta detrás de la barra
 * de navegación física o gestual del dispositivo.
 */
@Composable
fun NotiFastSnackbarHost(
    hostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    defaultSeverity: AlertSeverity = AlertSeverity.INFO
) {
    SnackbarHost(
        hostState = hostState,
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) { data: SnackbarData ->
        val visuals = data.visuals as? NotiFastSnackbarVisuals
        val severity = visuals?.severity ?: defaultSeverity
        val title = visuals?.title

        NotiFastAlertBanner(
            message = data.visuals.message,
            title = title,
            severity = severity,
            actionLabel = data.visuals.actionLabel,
            onAction = { data.performAction() },
            onDismiss = { data.dismiss() }
        )
    }
}

/**
 * Muestra un snackbar AAA formateado con [NotiFastAlertBanner] de manera tipada.
 */
suspend fun SnackbarHostState.showNotiFastSnackbar(
    message: String,
    severity: AlertSeverity = AlertSeverity.INFO,
    title: String? = null,
    actionLabel: String? = null,
    duration: SnackbarDuration = SnackbarDuration.Short
): SnackbarResult {
    return showSnackbar(
        NotiFastSnackbarVisuals(
            message = message,
            actionLabel = actionLabel,
            withDismissAction = true,
            duration = duration,
            severity = severity,
            title = title
        )
    )
}

/**
 * Hook composable reutilizable para emitir alertas AAA desde cualquier composable sin boilerplate de corrutinas.
 */
@Composable
fun rememberNotiFastAlert(): (message: String, severity: AlertSeverity) -> Unit {
    val hostState = LocalNotiFastSnackbarHostState.current
    val scope = rememberCoroutineScope()
    return remember(hostState, scope) {
        { message, severity ->
            scope.launch {
                hostState.currentSnackbarData?.dismiss()
                hostState.showNotiFastSnackbar(
                    message = message,
                    severity = severity
                )
            }
        }
    }
}
