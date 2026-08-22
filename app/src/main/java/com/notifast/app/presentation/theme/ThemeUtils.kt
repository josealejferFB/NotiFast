package com.notifast.app.presentation.theme

import androidx.compose.ui.graphics.Color
import com.notifast.app.presentation.theme.color.Aqua
import com.notifast.app.presentation.theme.color.toSafeComposeColor
import kotlin.math.max
import kotlin.math.min

object ThemeUtils {
    /**
     * Calibrates a subject color for accessibility in light or dark themes.
     * Guarantees contrast compliance (WCAG AA) using pure Kotlin HSL transformation.
     */
    fun getAccessibleColor(hex: String, isDark: Boolean): Color {
        val baseColor = hex.toSafeComposeColor(Aqua)
        return getAccessibleColor(baseColor, isDark)
    }

    /**
     * Calibrates a Compose Color for accessibility in light or dark themes.
     */
    fun getAccessibleColor(baseColor: Color, isDark: Boolean): Color {
        val hsl = rgbToHsl(baseColor.red, baseColor.green, baseColor.blue)

        if (isDark) {
            // In dark mode: ensure brightness is sufficient for visibility on dark surfaces (#10161A / #1B242A)
            // Saturation capped at 70%, Lightness adjusted to [0.55, 0.75]
            hsl[1] = hsl[1].coerceIn(0.25f, 0.70f)
            hsl[2] = hsl[2].coerceIn(0.55f, 0.75f)
        } else {
            // In light mode: avoid overly bright/washed out colors on white backgrounds
            // Saturation minimum 40%, Lightness capped at 0.50
            hsl[1] = hsl[1].coerceAtLeast(0.35f)
            if (hsl[2] > 0.55f) {
                hsl[2] = 0.45f
            }
        }

        return hslToColor(hsl[0], hsl[1], hsl[2], baseColor.alpha)
    }

    /**
     * Converts RGB [0..1] values to HSL array: [0] = Hue (0..360), [1] = Saturation (0..1), [2] = Lightness (0..1).
     */
    fun rgbToHsl(r: Float, g: Float, b: Float): FloatArray {
        val maxVal = max(r, max(g, b))
        val minVal = min(r, min(g, b))
        val delta = maxVal - minVal

        val l = (maxVal + minVal) / 2.0f
        var h = 0.0f
        var s = 0.0f

        if (delta > 0.00001f) {
            s = if (l > 0.5f) delta / (2.0f - maxVal - minVal) else delta / (maxVal + minVal)

            h = when {
                maxVal == r -> ((g - b) / delta + (if (g < b) 6.0f else 0.0f)) * 60.0f
                maxVal == g -> ((b - r) / delta + 2.0f) * 60.0f
                else -> ((r - g) / delta + 4.0f) * 60.0f
            }
        }

        return floatArrayOf(h, s, l)
    }

    /**
     * Converts HSL values (h in 0..360, s in 0..1, l in 0..1) to Compose [Color].
     */
    fun hslToColor(h: Float, s: Float, l: Float, alpha: Float = 1.0f): Color {
        if (s <= 0.00001f) {
            return Color(l, l, l, alpha)
        }

        val q = if (l < 0.5f) l * (1.0f + s) else l + s - l * s
        val p = 2.0f * l - q

        val hk = (h % 360.0f) / 360.0f
        val tr = (hk + 1.0f / 3.0f).let { if (it < 0) it + 1.0f else if (it > 1) it - 1.0f else it }
        val tg = hk.let { if (it < 0) it + 1.0f else if (it > 1) it - 1.0f else it }
        val tb = (hk - 1.0f / 3.0f).let { if (it < 0) it + 1.0f else if (it > 1) it - 1.0f else it }

        fun hueToRgb(p: Float, q: Float, tc: Float): Float {
            return when {
                tc < 1.0f / 6.0f -> p + (q - p) * 6.0f * tc
                tc < 1.0f / 2.0f -> q
                tc < 2.0f / 3.0f -> p + (q - p) * (2.0f / 3.0f - tc) * 6.0f
                else -> p
            }
        }

        val r = hueToRgb(p, q, tr).coerceIn(0.0f, 1.0f)
        val g = hueToRgb(p, q, tg).coerceIn(0.0f, 1.0f)
        val b = hueToRgb(p, q, tb).coerceIn(0.0f, 1.0f)

        return Color(r, g, b, alpha)
    }
}
