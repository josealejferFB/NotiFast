package com.example.notifast.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.notifast.ui.theme.Aqua
import com.example.notifast.ui.theme.Ruby

@Composable
fun StudyBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "floating")
    
    // Lista ampliada de iconos para mayor densidad visual
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
        Icons.Default.Abc,
        Icons.Default.AutoAwesome,
        Icons.Default.Architecture,
        Icons.Default.AutoGraph
    )

    Box(modifier = Modifier.fillMaxSize().background(
        Brush.verticalGradient(
            listOf(Color.White, Color(0xFFF8F9FA))
        )
    )) {
        studyIcons.forEachIndexed { index, icon ->
            val duration = 4000 + (index * 600)
            
            val yOffset by infiniteTransition.animateFloat(
                initialValue = 0f,
                targetValue = 40f,
                animationSpec = infiniteRepeatable(
                    animation = tween(duration, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "yOffset"
            )

            val rotation by infiniteTransition.animateFloat(
                initialValue = -10f,
                targetValue = 10f,
                animationSpec = infiniteRepeatable(
                    animation = tween(duration + 1200, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "rotation"
            )

            // Lógica de posicionamiento mejorada para cubrir toda la pantalla, especialmente la parte inferior
            val xPos = (index * 17) % 95
            // Distribuimos los iconos en el eje Y de forma más uniforme y extendida
            val yPos = (index * 31) % 100 

            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier
                    .offset(
                        x = (xPos.dp * 4), 
                        y = (yPos.dp * 10) + yOffset.dp // Aumentamos el multiplicador para llegar más abajo
                    )
                    .graphicsLayer(rotationZ = rotation)
                    .size(44.dp) // Un poco más grandes para que se noten
                    .alpha(0.04f), // Sutil para no distraer del contenido principal
                tint = if (index % 2 == 0) Ruby else Aqua
            )
        }
    }
}
