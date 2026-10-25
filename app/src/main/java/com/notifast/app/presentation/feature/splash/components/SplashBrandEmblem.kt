package com.notifast.app.presentation.feature.splash.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import kotlinx.coroutines.launch

@Composable
fun SplashBrandEmblem(
    isReducedMotion: Boolean,
    modifier: Modifier = Modifier
) {
    val scaleAnim = remember { Animatable(if (isReducedMotion) 1f else 0.72f) }
    val alphaAnim = remember { Animatable(if (isReducedMotion) 1f else 0f) }

    val infiniteTransition = rememberInfiniteTransition(label = "SplashAuraTransition")
    val auraScale by if (isReducedMotion) {
        remember { androidx.compose.runtime.mutableFloatStateOf(1f) }
    } else {
        infiniteTransition.animateFloat(
            initialValue = 0.94f,
            targetValue = 1.06f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "SplashAuraPulse"
        )
    }

    LaunchedEffect(isReducedMotion) {
        if (!isReducedMotion) {
            launch {
                scaleAnim.animateTo(
                    targetValue = 1f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
            }
            launch {
                alphaAnim.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(durationMillis = 450, easing = LinearOutSlowInEasing)
                )
            }
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val isDark = MaterialTheme.colorScheme.background.let {
        // WCAG AAA dark background check (relative luminance < 0.5f)
        (0.2126f * it.red + 0.7152f * it.green + 0.0722f * it.blue) < 0.5f
    }

    val auraBrush = remember(primaryColor, primaryContainer, isDark) {
        Brush.radialGradient(
            colors = listOf(
                primaryColor.copy(alpha = if (isDark) 0.32f else 0.20f),
                primaryContainer.copy(alpha = if (isDark) 0.16f else 0.08f),
                Color.Transparent
            )
        )
    }

    Box(
        modifier = modifier
            .size(240.dp)
            .graphicsLayer {
                scaleX = scaleAnim.value
                scaleY = scaleAnim.value
                alpha = alphaAnim.value
            },
        contentAlignment = Alignment.Center
    ) {
        // Reactive ambient aura glow
        Box(
            modifier = Modifier
                .size(230.dp)
                .scale(auraScale)
                .background(auraBrush)
        )

        // Tactile Squircle Container
        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(primaryContainer.copy(alpha = if (isDark) 0.22f else 0.14f))
                .border(
                    width = 1.5.dp,
                    color = primaryColor.copy(alpha = if (isDark) 0.35f else 0.25f),
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = stringResource(R.string.splash_logo_content_description),
                modifier = Modifier.size(80.dp)
            )
        }
    }
}
