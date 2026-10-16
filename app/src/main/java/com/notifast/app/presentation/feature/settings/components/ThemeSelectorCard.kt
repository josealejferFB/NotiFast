package com.notifast.app.presentation.feature.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifast.app.R
import com.notifast.app.domain.model.theme.ThemePreset
import com.notifast.app.presentation.atoms.NotiFastCard
import com.notifast.app.presentation.theme.color.toSafeComposeColor
import com.notifast.app.presentation.theme.isDark

/**
 * NotiFast 3.0 Theme & Palette Customizer Card.
 * Displays mode segments (System/Light/Dark), quick-selection preset swatches, and custom studio CTA.
 */
@Composable
fun ThemeSelectorCard(
    currentPresetId: String,
    customPrimaryHex: String?,
    isDarkMode: Boolean?,
    onSelectPreset: (String) -> Unit,
    onSetDarkMode: (Boolean?) -> Unit,
    onOpenCustomStudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val activePreset = remember(currentPresetId) { ThemePreset.fromId(currentPresetId) }
    val isSystemDark = MaterialTheme.colorScheme.isDark

    NotiFastCard(
        modifier = modifier.fillMaxWidth(),
        contentPadding = 18.dp,
        shape = RoundedCornerShape(20.dp),
        backgroundColor = MaterialTheme.colorScheme.surface,
        borderColor = MaterialTheme.colorScheme.outlineVariant
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = stringResource(R.string.settings_theme_card_title),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.settings_theme_card_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Luminance Mode Segment Switcher
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ModeSegmentButton(
                    text = stringResource(R.string.settings_theme_mode_system),
                    icon = Icons.Default.BrightnessAuto,
                    isSelected = isDarkMode == null,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSetDarkMode(null)
                    },
                    modifier = Modifier.weight(1f)
                )
                ModeSegmentButton(
                    text = stringResource(R.string.settings_theme_mode_light),
                    icon = Icons.Default.LightMode,
                    isSelected = isDarkMode == false,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSetDarkMode(false)
                    },
                    modifier = Modifier.weight(1f)
                )
                ModeSegmentButton(
                    text = stringResource(R.string.settings_theme_mode_dark),
                    icon = Icons.Default.DarkMode,
                    isSelected = isDarkMode == true,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSetDarkMode(true)
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Horizontal Swatches Selection Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                ThemePreset.availablePresets.forEach { preset ->
                    val isSelected = activePreset == preset
                    val primaryColor = remember(preset, isSystemDark) {
                        (if (isSystemDark) preset.primaryDarkHex else preset.primaryLightHex).toSafeComposeColor(Color.Red)
                    }
                    val secondaryColor = remember(preset, isSystemDark) {
                        (if (isSystemDark) preset.secondaryDarkHex else preset.secondaryLightHex).toSafeComposeColor(Color.Cyan)
                    }

                    PresetSwatchItem(
                        name = stringResource(preset.nameRes),
                        primaryColor = primaryColor,
                        secondaryColor = secondaryColor,
                        isSelected = isSelected,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSelectPreset(preset.id)
                        }
                    )
                }

                // Custom Studio Swatch
                val isCustomSelected = activePreset == ThemePreset.CUSTOM
                val customColor = remember(customPrimaryHex) {
                    customPrimaryHex?.toSafeComposeColor(Color.Magenta) ?: Color(0xFF9E1B32)
                }
                CustomSwatchItem(
                    color = customColor,
                    isSelected = isCustomSelected,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onOpenCustomStudio()
                    }
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Active theme description banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.6f))
                    .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "${stringResource(activePreset.nameRes)} • ${stringResource(activePreset.descriptionRes)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }
        }
    }
}
