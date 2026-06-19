package com.example.notifast.presentation.theme

import androidx.compose.ui.graphics.Color
import androidx.core.graphics.ColorUtils

object ThemeUtils {
    fun getAccessibleColor(hex: String, isDark: Boolean): Color {
        val baseColorInt = try {
            if (hex.isNotBlank()) android.graphics.Color.parseColor(hex)
            else android.graphics.Color.parseColor("#3B9797") // Aqua fallback
        } catch (e: Exception) {
            android.graphics.Color.parseColor("#3B9797")
        }

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
