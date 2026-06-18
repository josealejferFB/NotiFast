package com.example.notifast.presentation.feature.subjectdetail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.example.notifast.presentation.theme.color.Aqua

private fun parseSubjectColor(hex: String): Color {
    return try {
        if (hex.isNotBlank()) Color(android.graphics.Color.parseColor(hex))
        else Aqua
    } catch (e: Exception) { Aqua }
}

@Composable
fun SubjectDetailHero(
    subjectName: String,
    subjectIcon: String,
    subjectColorHex: String,
    noteCount: Int,
    evalCount: Int,
    onBackClick: () -> Unit
) {
    val color = parseSubjectColor(subjectColorHex)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        color,
                        color.copy(alpha = 0.7f),
                        color.copy(alpha = 0.2f),
                        color.copy(alpha = 0.2f),
                        MaterialTheme.colorScheme.background
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().align(Alignment.BottomCenter).padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(80.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surface),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = SubjectIconMapper.getIcon(subjectIcon),
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(44.dp)
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
