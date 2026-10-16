package com.notifast.app.presentation.feature.onboarding.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import com.notifast.app.presentation.theme.ThemeUtils
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import kotlin.math.PI
import kotlin.math.roundToInt
import kotlin.math.sin

private data class KnowledgeNode(
    val title: String,
    val icon: ImageVector,
    val color: Color,
    val offsetXdp: Float,
    val offsetYdp: Float
)

@Composable
fun AcademicNodesMotion(
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val infiniteTransition = rememberInfiniteTransition(label = "nodes_motion")

    // Flotación suave y respiración armónica
    val floatPhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "float_phase"
    )

    // Pulso viajero de paquetes de conocimiento
    val packetProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "packet_progress"
    )

    val isDark = isSystemInDarkTheme()
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    val tertiary = MaterialTheme.colorScheme.tertiary

    val nodes = remember(isDark, primary, secondary, tertiary) {
        listOf(
            KnowledgeNode(
                title = "Ciencias",
                icon = Icons.Default.Science,
                color = secondary,
                offsetXdp = -74f,
                offsetYdp = -46f
            ),
            KnowledgeNode(
                title = "Matemáticas",
                icon = Icons.Default.Calculate,
                color = primary,
                offsetXdp = 74f,
                offsetYdp = -46f
            ),
            KnowledgeNode(
                title = "Tecnología",
                icon = Icons.Default.Functions,
                color = tertiary,
                offsetXdp = -90f,
                offsetYdp = 6f
            ),
            KnowledgeNode(
                title = "Historia",
                icon = Icons.AutoMirrored.Filled.MenuBook,
                color = ThemeUtils.getAccessibleColor(Color(0xFFD97706), isDark),
                offsetXdp = 90f,
                offsetYdp = 6f
            ),
            KnowledgeNode(
                title = "Psicología",
                icon = Icons.Default.Psychology,
                color = ThemeUtils.getAccessibleColor(Color(0xFF4F46E5), isDark),
                offsetXdp = -58f,
                offsetYdp = 50f
            ),
            KnowledgeNode(
                title = "Artes",
                icon = Icons.Default.Architecture,
                color = ThemeUtils.getAccessibleColor(Color(0xFF9333EA), isDark),
                offsetXdp = 58f,
                offsetYdp = 50f
            )
        )
    }

    val outlineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (isDark) 0.45f else 0.35f)
    val logoScale = 0.985f + 0.025f * sin(floatPhase)

    Box(
        modifier = modifier.size(width = 240.dp, height = 148.dp),
        contentAlignment = Alignment.Center
    ) {
        // 1. Líneas de interconexión (Network Graph) y pulsos de datos
        Canvas(modifier = Modifier.matchParentSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)

            nodes.forEachIndexed { index, node ->
                val nodeDxPx = with(density) { node.offsetXdp.dp.toPx() }
                val nodeDyPx = with(density) {
                    (node.offsetYdp + sin(floatPhase + index * 1.05f) * 3f).dp.toPx()
                }
                val nodePos = Offset(center.x + nodeDxPx, center.y + nodeDyPx)

                // Línea de red sobria y sutil
                drawLine(
                    color = outlineColor,
                    start = center,
                    end = nodePos,
                    strokeWidth = with(density) { 1.dp.toPx() },
                    cap = StrokeCap.Round
                )

                // Pulso sutil de conocimiento que desemboca desde el logo hacia la materia
                val t = (packetProgress + index * (1f / nodes.size)) % 1f
                val clampedT = 0.22f + t * 0.70f
                val packetPos = Offset(
                    x = center.x + (nodePos.x - center.x) * clampedT,
                    y = center.y + (nodePos.y - center.y) * clampedT
                )
                val packetAlpha = (sin(t * PI).toFloat()).coerceIn(0f, 1f) * 0.75f

                drawCircle(
                    color = node.color.copy(alpha = packetAlpha),
                    radius = with(density) { 2.dp.toPx() },
                    center = packetPos
                )
            }
        }

        // 2. Nodo central: Isotipo NotiFast sobrio, limpio y sin degradados ruidosos
        Box(
            modifier = Modifier
                .size(66.dp)
                .graphicsLayer {
                    scaleX = logoScale
                    scaleY = logoScale
                }
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                contentScale = ContentScale.Fit
            )
        }

        // 3. Nodos perimetrales de asignaturas
        nodes.forEachIndexed { index, node ->
            val nodeDyDp = node.offsetYdp + sin(floatPhase + index * 1.05f) * 3f
            val nodeDxPx = with(density) { node.offsetXdp.dp.toPx() }
            val nodeDyPx = with(density) { nodeDyDp.dp.toPx() }

            Box(
                modifier = Modifier
                    .offset { IntOffset(nodeDxPx.roundToInt(), nodeDyPx.roundToInt()) }
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(
                        width = 1.dp,
                        color = node.color.copy(alpha = 0.8f),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(node.color.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = node.icon,
                        contentDescription = null,
                        tint = node.color,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
