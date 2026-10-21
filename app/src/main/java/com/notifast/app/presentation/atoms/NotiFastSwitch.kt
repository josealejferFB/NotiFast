package com.notifast.app.presentation.atoms

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

/**
 * NotiFast 3.2 Design System Reactive Switch Atom.
 * Applies reactive design tokens (onPrimary, primary, outline, surfaceVariant, outlineVariant)
 * and tactile haptic feedback on state changes.
 */
@Composable
fun NotiFastSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: SwitchColors = SwitchDefaults.colors(
        checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
        checkedTrackColor = MaterialTheme.colorScheme.primary,
        checkedBorderColor = MaterialTheme.colorScheme.primary,
        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
        uncheckedBorderColor = MaterialTheme.colorScheme.outlineVariant
    )
) {
    val haptic = LocalHapticFeedback.current

    Switch(
        checked = checked,
        onCheckedChange = if (onCheckedChange != null) {
            { newValue ->
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onCheckedChange(newValue)
            }
        } else null,
        modifier = modifier,
        enabled = enabled,
        colors = colors
    )
}
