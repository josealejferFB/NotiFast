package com.notifast.app.presentation.atoms

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Architecture
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Biotech
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.HistoryEdu
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * NotiFast 3.0 Tactile Paper / Studio Notebook Dynamic Background.
 * Renders a warm parchment or slate desk gradient with subtle floating educational watermarks.
 */
@Composable
fun NotiFastStudyBackground(
    modifier: Modifier = Modifier,
    showFloatingIcons: Boolean = true
) {
    val isDark = isSystemInDarkTheme()

    val studyIcons = listOf(
        Icons.AutoMirrored.Filled.MenuBook,
        Icons.Default.Edit,
        Icons.Default.School,
        Icons.Default.HistoryEdu,
        Icons.Default.Science,
        Icons.Default.AutoStories,
        Icons.Default.Lightbulb,
        Icons.Default.Calculate,
        Icons.Default.Functions,
        Icons.Default.Psychology,
        Icons.Default.Architecture,
        Icons.Default.Biotech,
        Icons.Default.Draw,
        Icons.Default.EmojiEvents,
        Icons.Default.Quiz,
        Icons.Default.Translate,
        Icons.Default.AutoAwesome,
        Icons.Default.AutoGraph
    )

    val bgColor1 = MaterialTheme.colorScheme.background
    val bgColor2 = MaterialTheme.colorScheme.surfaceVariant
    val iconAlpha = if (isDark) 0.05f else 0.035f

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(bgColor1, bgColor2)))
    ) {
        if (showFloatingIcons) {
            val infiniteTransition = rememberInfiniteTransition(label = "floating_bg_icons")
            studyIcons.forEachIndexed { index, icon ->
                val duration = 4500 + (index * 500)

            val yOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 35f,
                animationSpec = infiniteRepeatable(
                    animation = tween(duration, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "yOffset_$index"
            )

            val rotation by infiniteTransition.animateFloat(
                initialValue = -8f,
                targetValue = 8f,
                animationSpec = infiniteRepeatable(
                    animation = tween(duration + 1000, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "rotation_$index"
            )

            val xPos = (index * 19) % 92
            val yPos = (index * 29) % 96

            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .offset(x = (xPos.dp * 4), y = (yPos.dp * 10) + yOffset.dp)
                    .graphicsLayer(rotationZ = rotation)
                    .size(42.dp)
                    .alpha(iconAlpha),
                tint = if (index % 2 == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            )
        }
    }
}
}
