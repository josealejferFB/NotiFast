package com.notifast.app.presentation.feature.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.domain.model.theme.ThemePreset
import com.notifast.app.presentation.theme.color.toSafeComposeColor
import com.notifast.app.presentation.theme.isDark

/**
 * NotiFast 3.0 Seamless Appearance & Theme Customizer Component.
 * Flawlessly integrated into SettingsSectionContainer: zero nested card borders,
 * edge-to-edge LazyRow swatches, accessible controls, and WCAG AAA contrast.
 */
@Composable
fun ThemeSelectorCard(
    currentPresetId: String,
    currentBackgroundPresetId: String,
    customPrimaryHex: String?,
    customBackgroundHex: String? = null,
    isDarkMode: Boolean?,
    onSelectPreset: (String) -> Unit,
    onSelectBackgroundPreset: (String) -> Unit,
    onSetDarkMode: (Boolean?) -> Unit,
    onOpenCustomStudio: (CustomStudioTab) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val activePreset = remember(currentPresetId) { ThemePreset.fromId(currentPresetId) }
    val activeBackgroundPreset = remember(currentBackgroundPresetId) {
        com.notifast.app.domain.model.theme.BackgroundPreset.fromId(currentBackgroundPresetId)
    }
    val isSystemDark = MaterialTheme.colorScheme.isDark
    val defaultPrimaryFallback = MaterialTheme.colorScheme.primary
    val defaultSecondaryFallback = MaterialTheme.colorScheme.secondary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        // Luminance Mode Segment Switcher
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
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

        // Accent Palette Subtitle
        Text(
            text = stringResource(R.string.settings_theme_accent_title),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 14.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Horizontal Accent Swatches Selection Row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(ThemePreset.availablePresets, key = { it.id }) { preset ->
                val isSelected = activePreset == preset
                val primaryColor = remember(preset, isSystemDark) {
                    (if (isSystemDark) preset.primaryDarkHex else preset.primaryLightHex)
                        .toSafeComposeColor(defaultPrimaryFallback)
                }
                val secondaryColor = remember(preset, isSystemDark) {
                    (if (isSystemDark) preset.secondaryDarkHex else preset.secondaryLightHex)
                        .toSafeComposeColor(defaultSecondaryFallback)
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

            // Custom Color Studio Swatch Item
            item(key = "custom_studio_swatch") {
                val isCustomSelected = activePreset == ThemePreset.CUSTOM
                val customColor = remember(customPrimaryHex) {
                    customPrimaryHex?.toSafeComposeColor(defaultPrimaryFallback) ?: defaultPrimaryFallback
                }
                CustomSwatchItem(
                    color = customColor,
                    isSelected = isCustomSelected,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onOpenCustomStudio(CustomStudioTab.ACCENT)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Background / Canvas Palette Subtitle
        Text(
            text = stringResource(R.string.settings_theme_background_title),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 14.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Horizontal Background Swatches Selection Row
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(
                com.notifast.app.domain.model.theme.BackgroundPreset.availablePresets,
                key = { it.id }
            ) { bgPreset ->
                BackgroundSwatchItem(
                    preset = bgPreset,
                    isSelected = activeBackgroundPreset == bgPreset,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSelectBackgroundPreset(bgPreset.id)
                    }
                )
            }

            // Custom Background Studio Swatch Item
            item(key = "custom_background_swatch") {
                CustomBackgroundSwatchItem(
                    customBackgroundHex = customBackgroundHex,
                    isSelected = activeBackgroundPreset == com.notifast.app.domain.model.theme.BackgroundPreset.CUSTOM,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onOpenCustomStudio(CustomStudioTab.BACKGROUND)
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Active theme contextual description banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f))
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                text = "${stringResource(activePreset.nameRes)} • ${stringResource(activeBackgroundPreset.nameRes)}: ${stringResource(activeBackgroundPreset.descriptionRes)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
