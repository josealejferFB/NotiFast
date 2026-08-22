package com.example.notifast.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.graphics.ColorUtils
import com.example.notifast.presentation.theme.color.Aqua
import com.example.notifast.presentation.theme.color.toSafeComposeColor

object ThemeUtils {
    /**
     * Calibrates a subject color for accessibility in light or dark themes.
     */
    fun getAccessibleColor(hex: String, isDark: Boolean): Color {
        val baseColor = hex.toSafeComposeColor(Aqua)
        val baseColorInt = baseColor.toArgb()

        val hsl = FloatArray(3)
        ColorUtils.colorToHSL(baseColorInt, hsl)

        if (isDark) {
            // Desaturate and reduce lightness for dark mode
            hsl[1] = hsl[1].coerceAtMost(0.6f) // Max saturation 60%
            hsl[2] = hsl[2].coerceAtMost(0.5f) // Max lightness 50%
        } else {
            // Darken extremely light colors for light mode
            if (hsl[2] > 0.8f) {
                hsl[2] = 0.6f 
            }
        }

        return Color(ColorUtils.HSLToColor(hsl))
    }
}
