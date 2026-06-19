package com.example.notifast.presentation.feature.subjectdetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.notifast.presentation.feature.notes.components.SubjectIconMapper
import com.example.notifast.presentation.theme.ThemeUtils

@Composable
fun SubjectDetailHero(
    subjectName: String,
    subjectIcon: String,
    subjectColorHex: String,
    noteCount: Int,
    evalCount: Int,
    onBackClick: () -> Unit
) {
    val isDark = isSystemInDarkTheme()
    val color = ThemeUtils.getAccessibleColor(subjectColorHex, isDark)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(Color.Transparent)
    ) {
        // Radial Glow
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.radialGradient(
                        colors = listOf(color.copy(alpha = 0.2f), Color.Transparent),
                        radius = 400f
                    )
                )
        )

        Column(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Glassmorphism Icon Container
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f))
                    .border(1.dp, color.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = SubjectIconMapper.getIcon(subjectIcon),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = subjectName,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "$noteCount apuntes · $evalCount evaluaciones",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
