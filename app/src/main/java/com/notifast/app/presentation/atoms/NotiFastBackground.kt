package com.notifast.app.presentation.atoms

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color

@Composable
fun NotiFastBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val particles = rememberParticles()

    val alphas = particles.map { particle ->
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = particle.duration, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha_${particle.id}"
        )
    }

    Canvas(modifier = modifier.fillMaxSize()) {
        particles.forEachIndexed { index, particle ->
            val alphaValue = alphas[index].value
            drawCircle(
                color = Color(particle.color).copy(alpha = alphaValue * 0.12f),
                radius = particle.size,
                center = Offset(
                    x = particle.baseX * this.size.width,
                    y = particle.baseY * this.size.height
                )
            )
        }
    }
}

private data class Particle(
    val id: Int,
    val baseX: Float,
    val baseY: Float,
    val size: Float,
    val duration: Int,
    val color: Long
)

@Composable
private fun rememberParticles(): List<Particle> {
    return remember {
        listOf(
            Particle(0, 0.1f, 0.1f, 30f, 3000, 0xFF9E1B32), // Muted Crimson
            Particle(1, 0.9f, 0.2f, 25f, 4000, 0xFF2A7B7B), // Aqua Green
            Particle(2, 0.2f, 0.8f, 35f, 3500, 0xFF15803D), // Forest Sage
            Particle(3, 0.8f, 0.85f, 20f, 4500, 0xFF9E1B32),
            Particle(4, 0.5f, 0.5f, 40f, 5000, 0xFF2A7B7B),
            Particle(5, 0.15f, 0.5f, 20f, 2800, 0xFF15803D),
            Particle(6, 0.7f, 0.4f, 28f, 3800, 0xFF9E1B32),
            Particle(7, 0.35f, 0.7f, 22f, 4200, 0xFF2A7B7B)
        )
    }
}
