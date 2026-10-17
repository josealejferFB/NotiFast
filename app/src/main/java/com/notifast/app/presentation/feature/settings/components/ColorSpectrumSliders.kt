package com.notifast.app.presentation.feature.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.presentation.theme.ThemeUtils

/**
 * Reusable touch-sensitive slider bar with high-contrast tactile thumb indicator.
 * Provides unified, conflict-free tap & drag gesture tracking via awaitEachGesture.
 */
@Composable
fun SpectrumSliderBar(
    value: Float,
    onValueChanged: (Float) -> Unit,
    trackBrush: Brush,
    thumbColor: Color,
    contentDescription: String,
    modifier: Modifier = Modifier
) {
    var barWidthPx by remember { mutableFloatStateOf(1f) }
    val density = LocalDensity.current
    val thumbSizeDp = 24.dp
    val thumbSizePx = with(density) { thumbSizeDp.toPx() }
    val currentOnValueChanged by rememberUpdatedState(onValueChanged)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(34.dp)
            .semantics { this.contentDescription = contentDescription }
            .onSizeChanged { barWidthPx = it.width.toFloat().coerceAtLeast(1f) }
            .pointerInput(contentDescription) {
                awaitEachGesture {
                    val down = awaitFirstDown()
                    down.consume()
                    val availableWidth = (barWidthPx - thumbSizePx).coerceAtLeast(1f)
                    val tapVal = ((down.position.x - (thumbSizePx / 2f)) / availableWidth).coerceIn(0f, 1f)
                    currentOnValueChanged(tapVal)
                    val pointerId = down.id
                    while (true) {
                        val event = awaitPointerEvent()
                        val change = event.changes.firstOrNull { it.id == pointerId } ?: break
                        if (!change.pressed) break
                        change.consume()
                        val dragVal = ((change.position.x - (thumbSizePx / 2f)) / availableWidth).coerceIn(0f, 1f)
                        currentOnValueChanged(dragVal)
                    }
                }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        // Track
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(trackBrush)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f),
                    shape = RoundedCornerShape(10.dp)
                )
        )

        // Tactile Thumb
        val availableWidth = (barWidthPx - thumbSizePx).coerceAtLeast(0f)
        val thumbOffsetPx = (value.coerceIn(0f, 1f) * availableWidth).toInt()

        Box(
            modifier = Modifier
                .offset { IntOffset(thumbOffsetPx, 0) }
                .size(thumbSizeDp)
                .shadow(2.dp, CircleShape)
                .clip(CircleShape)
                .background(Color.White)
                .border(3.dp, thumbColor, CircleShape)
                .border(1.dp, Color.White, CircleShape)
        )
    }
}

/**
 * Continuous touch-sensitive spectrum hue bar (0° to 360°).
 */
@Composable
fun SpectrumHueBar(
    hue: Float,
    onHueChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val rainbowBrush = remember {
        Brush.horizontalGradient(
            listOf(
                Color.Red, Color.Yellow, Color.Green,
                Color.Cyan, Color.Blue, Color.Magenta, Color.Red
            )
        )
    }
    val currentThumbColor = remember(hue) {
        ThemeUtils.hslToColor(hue, 1f, 0.5f)
    }

    SpectrumSliderBar(
        value = (hue / 360f).coerceIn(0f, 1f),
        onValueChanged = { onHueChanged(it * 360f) },
        trackBrush = rainbowBrush,
        thumbColor = currentThumbColor,
        contentDescription = stringResource(R.string.settings_theme_hex_hue_label),
        modifier = modifier
    )
}

/**
 * Continuous saturation bar (0% to 100%) for the selected hue and lightness.
 */
@Composable
fun SaturationBar(
    hue: Float,
    saturation: Float,
    lightness: Float,
    onSaturationChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val satBrush = remember(hue, lightness) {
        val previewLight = lightness.coerceIn(0.25f, 0.75f)
        val lowSat = ThemeUtils.hslToColor(hue, 0.02f, previewLight)
        val highSat = ThemeUtils.hslToColor(hue, 1.0f, previewLight)
        Brush.horizontalGradient(listOf(lowSat, highSat))
    }
    val currentThumbColor = remember(hue, saturation) {
        ThemeUtils.hslToColor(hue, saturation, 0.5f)
    }

    SpectrumSliderBar(
        value = saturation.coerceIn(0f, 1f),
        onValueChanged = onSaturationChanged,
        trackBrush = satBrush,
        thumbColor = currentThumbColor,
        contentDescription = stringResource(R.string.settings_theme_hex_saturation_label),
        modifier = modifier
    )
}

/**
 * Continuous lightness/intensity bar (0% to 100%) for the selected hue and saturation.
 */
@Composable
fun LightnessBar(
    hue: Float,
    saturation: Float,
    lightness: Float,
    onLightnessChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val lightBrush = remember(hue, saturation) {
        val pureTone = ThemeUtils.hslToColor(hue, saturation.coerceAtLeast(0.40f), 0.50f)
        Brush.horizontalGradient(listOf(Color.Black, pureTone, Color.White))
    }
    val currentThumbColor = remember(hue, saturation, lightness) {
        ThemeUtils.hslToColor(hue, saturation, lightness)
    }

    SpectrumSliderBar(
        value = lightness.coerceIn(0f, 1f),
        onValueChanged = onLightnessChanged,
        trackBrush = lightBrush,
        thumbColor = currentThumbColor,
        contentDescription = stringResource(R.string.settings_theme_hex_lightness_label),
        modifier = modifier
    )
}
