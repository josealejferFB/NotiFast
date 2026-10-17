package com.notifast.app.presentation.feature.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.notifast.app.R
import com.notifast.app.presentation.theme.ThemeUtils
import com.notifast.app.presentation.theme.color.toSafeComposeColor
import java.util.Locale

/**
 * Free-form Hex & Spectrum Color Picker.
 * Gives students complete freedom to select ANY color via continuous Hue, Saturation,
 * and Lightness touch sliders or by typing/pasting direct 6-digit hex values.
 * Strictly 0 presets.
 */
@Composable
fun HexColorStudioPicker(
    currentHex: String,
    onColorChanged: (String) -> Unit,
    isBackgroundMode: Boolean,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val initialColor = remember { currentHex.toSafeComposeColor(Color.Gray) }
    val initialHsl = remember { ThemeUtils.rgbToHsl(initialColor.red, initialColor.green, initialColor.blue) }

    var currentHue by remember { mutableFloatStateOf(initialHsl[0]) }
    var currentSaturation by remember { mutableFloatStateOf(initialHsl[1]) }
    var currentLightness by remember { mutableFloatStateOf(initialHsl[2]) }

    var textInput by remember { mutableStateOf(currentHex.removePrefix("#").uppercase(Locale.ROOT)) }
    var lastEmittedHex by remember {
        val clean = currentHex.removePrefix("#").uppercase(Locale.ROOT)
        mutableStateOf(if (clean.isNotEmpty()) "#$clean" else "")
    }

    val activeColor = remember(currentHue, currentSaturation, currentLightness) {
        ThemeUtils.hslToColor(currentHue, currentSaturation, currentLightness)
    }

    fun emitFromSliders(newHue: Float, newSat: Float, newLight: Float) {
        val hex = ThemeUtils.hslToColor(newHue, newSat, newLight).toHexCode()
        lastEmittedHex = hex
        textInput = hex.removePrefix("#")
        onColorChanged(hex)
    }

    LaunchedEffect(currentHex) {
        val clean = currentHex.removePrefix("#").uppercase(Locale.ROOT)
        val normalized = "#$clean"
        if (clean.length == 6 && normalized != lastEmittedHex) {
            val parsed = currentHex.toSafeComposeColor(Color.Gray)
            val hsl = ThemeUtils.rgbToHsl(parsed.red, parsed.green, parsed.blue)
            if (parsed.red != parsed.green || parsed.green != parsed.blue) currentHue = hsl[0]
            currentSaturation = hsl[1]
            currentLightness = hsl[2]
            textInput = clean
            lastEmittedHex = normalized
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        // Direct Hex Text Input & Live Swatch Card
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(activeColor)
                    .border(
                        width = 2.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "#",
                    color = if (currentLightness > 0.55f) Color.Black.copy(alpha = 0.65f) else Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            }

            OutlinedTextField(
                value = textInput,
                onValueChange = { input ->
                    val filtered = input.filter { it.isLetterOrDigit() }.take(6).uppercase(Locale.ROOT)
                    textInput = filtered
                    if (filtered.length == 6 && filtered.all { it in "0123456789ABCDEF" }) {
                        val hex = "#$filtered"
                        lastEmittedHex = hex
                        val parsed = hex.toSafeComposeColor(Color.Gray)
                        val hsl = ThemeUtils.rgbToHsl(parsed.red, parsed.green, parsed.blue)
                        if (parsed.red != parsed.green || parsed.green != parsed.blue) {
                            currentHue = hsl[0]
                        }
                        currentSaturation = hsl[1]
                        currentLightness = hsl[2]
                        onColorChanged(hex)
                    }
                },
                modifier = Modifier.weight(1f),
                label = { Text(stringResource(R.string.settings_theme_hex_input_label)) },
                prefix = {
                    Text(
                        text = "#",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Spectrum Hue Slider (0° - 360°)
        SliderHeader(
            label = stringResource(R.string.settings_theme_hex_hue_label),
            valueText = "${currentHue.toInt()}°"
        )
        Spacer(modifier = Modifier.height(6.dp))
        SpectrumHueBar(
            hue = currentHue,
            onHueChanged = {
                currentHue = it
                emitFromSliders(it, currentSaturation, currentLightness)
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Saturation Slider (0% - 100%)
        SliderHeader(
            label = stringResource(R.string.settings_theme_hex_saturation_label),
            valueText = "${(currentSaturation * 100).toInt()}%"
        )
        Spacer(modifier = Modifier.height(6.dp))
        SaturationBar(
            hue = currentHue,
            saturation = currentSaturation,
            lightness = currentLightness,
            onSaturationChanged = {
                currentSaturation = it
                emitFromSliders(currentHue, it, currentLightness)
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Lightness Slider (0% - 100%)
        SliderHeader(
            label = stringResource(R.string.settings_theme_hex_lightness_label),
            valueText = "${(currentLightness * 100).toInt()}%"
        )
        Spacer(modifier = Modifier.height(6.dp))
        LightnessBar(
            hue = currentHue,
            saturation = currentSaturation,
            lightness = currentLightness,
            onLightnessChanged = {
                currentLightness = it
                emitFromSliders(currentHue, currentSaturation, it)
            }
        )
    }
}

@Composable
private fun SliderHeader(label: String, valueText: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = valueText,
            style = MaterialTheme.typography.labelSmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )
    }
}

private fun Color.toHexCode(): String {
    val r = (red * 255).toInt().coerceIn(0, 255)
    val g = (green * 255).toInt().coerceIn(0, 255)
    val b = (blue * 255).toInt().coerceIn(0, 255)
    return String.format(Locale.ROOT, "#%02X%02X%02X", r, g, b)
}
