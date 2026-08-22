package com.notifast.app.presentation.feature.addsubject.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.notifast.app.presentation.feature.notes.components.SubjectIconMapper
import com.notifast.app.presentation.theme.color.Aqua

@Composable
fun SubjectPreviewBadge(
    iconName: String,
    hexColor: String,
    modifier: Modifier = Modifier
) {
    val targetColor = try {
        Color(android.graphics.Color.parseColor(hexColor))
    } catch (e: Exception) {
        Aqua
    }

    val animatedColor by animateColorAsState(
        targetValue = targetColor,
        animationSpec = tween(durationMillis = 350),
        label = "SubjectPreviewColorAnimation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(animatedColor)
                .border(
                    BorderStroke(2.dp, Color.White.copy(alpha = 0.6f)),
                    RoundedCornerShape(28.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = SubjectIconMapper.getIcon(iconName),
                contentDescription = iconName,
                tint = Color.White,
                modifier = Modifier.size(44.dp)
            )
        }
    }
}
