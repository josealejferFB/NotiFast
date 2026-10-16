package com.notifast.app.presentation.feature.settings.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.domain.model.theme.ThemePreset
import com.notifast.app.presentation.atoms.NotiFastButton
import com.notifast.app.presentation.theme.ThemePaletteGenerator
import com.notifast.app.presentation.theme.color.toSafeComposeColor

private val AcademicStudioHues = listOf(
    "#BF092F", // Crimson Ruby
    "#E11D48", // Rose Coral
    "#C2410C", // Warm Terracotta
    "#D97706", // Amber Ochre
    "#15803D", // Forest Emerald
    "#0D9488", // Teal Studio
    "#0284C7", // Sky Cerulean
    "#2563EB", // Cobalt Royal
    "#4F46E5", // Electric Indigo
    "#7C3AED", // Deep Violet
    "#9333EA", // Purple Orchid
    "#475569"  // Slate Obsidian
)

/**
 * Custom Palette Studio modal bottom sheet.
 * Enables students to pick an academic base color and previews the generated M3 scheme live.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CustomThemePaletteSheet(
    isOpen: Boolean,
    draftHex: String,
    isDark: Boolean,
    onColorSelected: (String) -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!isOpen) return

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val haptic = LocalHapticFeedback.current

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = stringResource(R.string.settings_theme_custom_sheet_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = stringResource(R.string.settings_theme_custom_sheet_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalArrangement = Arrangement.spacedBy(10.dp),
                maxItemsInEachRow = 6
            ) {
                AcademicStudioHues.forEach { hex ->
                    val isSelected = hex.equals(draftHex, ignoreCase = true)
                    val swatchColor = remember(hex) { hex.toSafeComposeColor(Color.Gray) }
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.15f else 1.0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        ),
                        label = "swatch_scale"
                    )

                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .scale(scale)
                            .clip(CircleShape)
                            .background(swatchColor)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else Color.White.copy(alpha = 0.3f),
                                shape = CircleShape
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onColorSelected(hex)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AnimatedVisibility(visible = isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val previewColorScheme = remember(draftHex, isDark) {
                ThemePaletteGenerator.generateColorScheme(
                    preset = ThemePreset.CUSTOM,
                    isDark = isDark,
                    customPrimaryHex = draftHex
                )
            }

            ThemePreviewMiniCard(previewColorScheme = previewColorScheme)

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = stringResource(R.string.btn_cancel))
                }

                NotiFastButton(
                    text = stringResource(R.string.settings_theme_custom_apply_btn),
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onApply()
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
