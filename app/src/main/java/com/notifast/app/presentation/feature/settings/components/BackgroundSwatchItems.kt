package com.notifast.app.presentation.feature.settings.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.domain.model.theme.BackgroundPreset
import com.notifast.app.presentation.theme.color.BackgroundDark
import com.notifast.app.presentation.theme.color.BackgroundLight
import com.notifast.app.presentation.theme.color.SurfaceContainerDark
import com.notifast.app.presentation.theme.color.SurfaceContainerLight
import com.notifast.app.presentation.theme.color.toSafeComposeColor
import com.notifast.app.presentation.theme.isDark

/**
 * Circular swatch item for selecting an independent academic background/canvas preset.
 * Follows Tactile Bento aesthetics with bounce animation and WCAG AAA touch targets.
 */
@Composable
fun BackgroundSwatchItem(
    preset: BackgroundPreset,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isSystemDark = MaterialTheme.colorScheme.isDark
    val bgFallback = if (isSystemDark) BackgroundDark else BackgroundLight
    val containerFallback = if (isSystemDark) SurfaceContainerDark else SurfaceContainerLight

    val bgColor = remember(preset, isSystemDark) {
        (if (isSystemDark) preset.backgroundDarkHex else preset.backgroundLightHex)
            .toSafeComposeColor(bgFallback)
    }
    val containerColor = remember(preset, isSystemDark) {
        (if (isSystemDark) preset.surfaceContainerDarkHex else preset.surfaceContainerLightHex)
            .toSafeComposeColor(containerFallback)
    }

    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.12f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "bg_swatch_scale"
    )

    val presetName = stringResource(preset.nameRes)
    val a11yDesc = stringResource(
        if (isSelected) R.string.bg_swatch_selected_cd else R.string.bg_swatch_unselected_cd,
        presetName
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .width(66.dp)
            .padding(vertical = 4.dp)
            .selectable(
                selected = isSelected,
                role = Role.RadioButton,
                onClick = onClick
            )
            .semantics { contentDescription = a11yDesc }
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(bgColor, containerColor)))
                .border(
                    width = if (isSelected) 3.dp else 1.dp,
                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = presetName,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
