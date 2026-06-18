package com.example.notifast.presentation.feature.subjectdetail.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.notifast.presentation.theme.color.Aqua

private fun parseSubjectColor(hex: String): Color {
    return try {
        if (hex.isNotBlank()) Color(android.graphics.Color.parseColor(hex))
        else Aqua
    } catch (e: Exception) { Aqua }
}

@Composable
fun SubjectDetailProgress(
    progressPercent: Float,
    completedCount: Int,
    totalCount: Int,
    subjectColorHex: String
) {
    val color = parseSubjectColor(subjectColorHex)
    val animatedProgress by animateFloatAsState(
        targetValue = progressPercent,
        animationSpec = tween(durationMillis = 800),
        label = "progress"
    )
    val pendingCount = totalCount - completedCount

    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
        HorizontalDivider(color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
        Spacer(Modifier.height(16.dp))
        Text("Progreso general", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(20.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(animatedProgress).fillMaxHeight().clip(RoundedCornerShape(10.dp)).background(color)
            )
        }
        Spacer(Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("${(progressPercent * 100).toInt()}%", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.ExtraBold, color = color)
                Text(if (progressPercent >= 1f) "¡Completaste todo!" else "Has completado el ${(progressPercent * 100).toInt()}% de tus evaluaciones", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.End) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(color))
                    Spacer(Modifier.width(6.dp))
                    Text("$completedCount completadas", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)))
                    Spacer(Modifier.width(6.dp))
                    Text("$pendingCount pendientes", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
        Spacer(Modifier.height(24.dp))
    }
}
