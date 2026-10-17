package com.notifast.app.presentation.feature.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.domain.model.theme.BackgroundPreset
import com.notifast.app.domain.model.theme.ThemePreset
import com.notifast.app.presentation.atoms.NotiFastButton
import com.notifast.app.presentation.theme.ThemePaletteGenerator
import kotlinx.coroutines.launch

/**
 * Custom Palette Studio modal bottom sheet.
 * Enables students to freely customize their academic accent hue and background canvas tone
 * via smooth continuous touch sliders and direct hex inputs, without preset constraints.
 * Previews live color scheme calibrated for WCAG AAA contrast compliance.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomThemePaletteSheet(
    draftHex: String,
    draftBackgroundHex: String,
    isDark: Boolean,
    onColorSelected: (String) -> Unit,
    onBackgroundHexSelected: (String) -> Unit,
    onApply: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    initialTab: CustomStudioTab = CustomStudioTab.ACCENT
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = false)
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    var selectedTab by rememberSaveable { mutableStateOf(initialTab) }

    val handleDismiss: () -> Unit = {
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            if (!sheetState.isVisible) {
                onDismiss()
            }
        }
    }

    val handleApply: () -> Unit = {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        scope.launch {
            sheetState.hide()
        }.invokeOnCompletion {
            onApply()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp)
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

            CustomStudioSegmentedTabs(
                selectedTab = selectedTab,
                onTabSelected = { selectedTab = it }
            )

            Spacer(modifier = Modifier.height(18.dp))

            when (selectedTab) {
                CustomStudioTab.ACCENT -> {
                    key(CustomStudioTab.ACCENT) {
                        HexColorStudioPicker(
                            currentHex = draftHex,
                            onColorChanged = onColorSelected,
                            isBackgroundMode = false
                        )
                    }
                }
                CustomStudioTab.BACKGROUND -> {
                    key(CustomStudioTab.BACKGROUND) {
                        HexColorStudioPicker(
                            currentHex = draftBackgroundHex,
                            onColorChanged = onBackgroundHexSelected,
                            isBackgroundMode = true
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            val previewColorScheme = remember(draftHex, draftBackgroundHex, isDark) {
                ThemePaletteGenerator.generateColorScheme(
                    preset = ThemePreset.CUSTOM,
                    isDark = isDark,
                    customPrimaryHex = draftHex,
                    backgroundPreset = BackgroundPreset.CUSTOM,
                    customBackgroundHex = draftBackgroundHex
                )
            }

            ThemePreviewMiniCard(previewColorScheme = previewColorScheme)

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = handleDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = stringResource(R.string.btn_cancel))
                }

                NotiFastButton(
                    text = stringResource(R.string.settings_theme_custom_apply_btn),
                    onClick = handleApply,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
