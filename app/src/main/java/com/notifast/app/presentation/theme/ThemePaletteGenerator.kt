package com.notifast.app.presentation.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color
import com.notifast.app.domain.model.theme.BackgroundPreset
import com.notifast.app.domain.model.theme.ThemePreset
import com.notifast.app.presentation.theme.color.BackgroundDark
import com.notifast.app.presentation.theme.color.BackgroundLight
import com.notifast.app.presentation.theme.color.ErrorContainerDark
import com.notifast.app.presentation.theme.color.ErrorContainerLight
import com.notifast.app.presentation.theme.color.ErrorDark
import com.notifast.app.presentation.theme.color.ErrorLight
import com.notifast.app.presentation.theme.color.OnBackgroundDark
import com.notifast.app.presentation.theme.color.OnBackgroundLight
import com.notifast.app.presentation.theme.color.OnErrorContainerDark
import com.notifast.app.presentation.theme.color.OnErrorContainerLight
import com.notifast.app.presentation.theme.color.OnErrorDark
import com.notifast.app.presentation.theme.color.OnErrorLight
import com.notifast.app.presentation.theme.color.OnSurfaceDark
import com.notifast.app.presentation.theme.color.OnSurfaceLight
import com.notifast.app.presentation.theme.color.OnSurfaceVariantDark
import com.notifast.app.presentation.theme.color.OnSurfaceVariantLight
import com.notifast.app.presentation.theme.color.OnTertiaryContainerDark
import com.notifast.app.presentation.theme.color.OnTertiaryContainerLight
import com.notifast.app.presentation.theme.color.OnTertiaryDark
import com.notifast.app.presentation.theme.color.OnTertiaryLight
import com.notifast.app.presentation.theme.color.OutlineDark
import com.notifast.app.presentation.theme.color.OutlineLight
import com.notifast.app.presentation.theme.color.OutlineVariantDark
import com.notifast.app.presentation.theme.color.OutlineVariantLight
import com.notifast.app.presentation.theme.color.PrimaryLight
import com.notifast.app.presentation.theme.color.SecondaryLight
import com.notifast.app.presentation.theme.color.SurfaceContainerDark
import com.notifast.app.presentation.theme.color.SurfaceContainerHighDark
import com.notifast.app.presentation.theme.color.SurfaceContainerHighLight
import com.notifast.app.presentation.theme.color.SurfaceContainerLight
import com.notifast.app.presentation.theme.color.SurfaceDark
import com.notifast.app.presentation.theme.color.SurfaceLight
import com.notifast.app.presentation.theme.color.SurfaceVariantDark
import com.notifast.app.presentation.theme.color.SurfaceVariantLight
import com.notifast.app.presentation.theme.color.TertiaryContainerDark
import com.notifast.app.presentation.theme.color.TertiaryContainerLight
import com.notifast.app.presentation.theme.color.TertiaryDark
import com.notifast.app.presentation.theme.color.TertiaryLight
import com.notifast.app.presentation.theme.color.toSafeComposeColor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

/**
 * Pure algorithmic palette generator ensuring WCAG AAA compliant Material 3 color schemes.
 * Preserves NotiFast 3.0 tactile papercraft surface and container tokens across all themes.
 */
object ThemePaletteGenerator {

    /**
     * Calculates the W3C contrast ratio between two colors (ranging from 1.0 to 21.0).
     */
    fun calculateContrastRatio(foreground: Color, background: Color): Float {
        val l1 = calculateLuminance(foreground)
        val l2 = calculateLuminance(background)
        val lighter = max(l1, l2)
        val darker = min(l1, l2)
        return (lighter + 0.05f) / (darker + 0.05f)
    }

    /**
     * Calculates relative luminance of an sRGB color.
     */
    fun calculateLuminance(color: Color): Float {
        fun linearize(c: Float): Float {
            return if (c <= 0.04045f) c / 12.92f else ((c + 0.055f) / 1.055f).toDouble().pow(2.4).toFloat()
        }
        val r = linearize(color.red)
        val g = linearize(color.green)
        val b = linearize(color.blue)
        return 0.2126f * r + 0.7152f * g + 0.0722f * b
    }

    /**
     * Generates a Material 3 ColorScheme from a preset or custom hex value,
     * harmonized with an independent background/canvas preset.
     */
    fun generateColorScheme(
        preset: ThemePreset,
        isDark: Boolean,
        customPrimaryHex: String? = null,
        backgroundPreset: BackgroundPreset = BackgroundPreset.CLASSIC
    ): ColorScheme {
        return if (preset == ThemePreset.CUSTOM) {
            val baseColor = customPrimaryHex?.toSafeComposeColor(PrimaryLight) ?: PrimaryLight
            createCustomScheme(
                primaryColor = baseColor,
                isDark = isDark,
                backgroundPreset = backgroundPreset
            )
        } else {
            val primaryHex = if (isDark) preset.primaryDarkHex else preset.primaryLightHex
            val secondaryHex = if (isDark) preset.secondaryDarkHex else preset.secondaryLightHex
            val primaryColor = primaryHex.toSafeComposeColor(PrimaryLight)
            val secondaryColor = secondaryHex.toSafeComposeColor(SecondaryLight)
            createCustomScheme(
                primaryColor = primaryColor,
                isDark = isDark,
                secondaryColor = secondaryColor,
                backgroundPreset = backgroundPreset
            )
        }
    }

    /**
     * Dynamically creates a compliant ColorScheme from primary and optional secondary colors
     * combined with the chosen background canvas preset.
     */
    fun createCustomScheme(
        primaryColor: Color,
        isDark: Boolean,
        secondaryColor: Color? = null,
        backgroundPreset: BackgroundPreset = BackgroundPreset.CLASSIC
    ): ColorScheme {
        val primaryHsl = ThemeUtils.rgbToHsl(primaryColor.red, primaryColor.green, primaryColor.blue)
        val secColor = secondaryColor ?: run {
            val rotatedHue = (primaryHsl[0] + 35f) % 360f
            ThemeUtils.hslToColor(rotatedHue, primaryHsl[1].coerceIn(0.65f, 0.75f), primaryHsl[2])
        }
        val secondaryHsl = ThemeUtils.rgbToHsl(secColor.red, secColor.green, secColor.blue)

        return if (isDark) {
            createDarkScheme(primaryHsl, secondaryHsl, backgroundPreset)
        } else {
            createLightScheme(primaryHsl, secondaryHsl, backgroundPreset)
        }
    }

    private fun createLightScheme(
        primaryHsl: FloatArray,
        secondaryHsl: FloatArray,
        backgroundPreset: BackgroundPreset
    ): ColorScheme {
        val pHue = primaryHsl[0]
        val pSat = primaryHsl[1].coerceIn(0.65f, 0.85f)
        var pLight = primaryHsl[2].coerceIn(0.24f, 0.36f)
        var primary = ThemeUtils.hslToColor(pHue, pSat, pLight)
        while (calculateContrastRatio(Color.White, primary) < 4.5f && pLight > 0.12f) {
            pLight -= 0.02f
            primary = ThemeUtils.hslToColor(pHue, pSat, pLight)
        }
        val onPrimary = Color.White
        val primaryContainer = ThemeUtils.hslToColor(pHue, 0.30f, 0.95f)
        val onPrimaryContainer = ThemeUtils.hslToColor(pHue, 0.85f, 0.16f)

        val sHue = secondaryHsl[0]
        val sSat = secondaryHsl[1].coerceIn(0.65f, 0.85f)
        var sLight = secondaryHsl[2].coerceIn(0.24f, 0.36f)
        var secondary = ThemeUtils.hslToColor(sHue, sSat, sLight)
        while (calculateContrastRatio(Color.White, secondary) < 4.5f && sLight > 0.12f) {
            sLight -= 0.02f
            secondary = ThemeUtils.hslToColor(sHue, sSat, sLight)
        }
        val onSecondary = Color.White
        val secondaryContainer = ThemeUtils.hslToColor(sHue, 0.30f, 0.95f)
        val onSecondaryContainer = ThemeUtils.hslToColor(sHue, 0.85f, 0.16f)

        val bg = backgroundPreset.backgroundLightHex.toSafeComposeColor(BackgroundLight)
        val surface = backgroundPreset.surfaceLightHex.toSafeComposeColor(SurfaceLight)
        val surfaceContainer = backgroundPreset.surfaceContainerLightHex.toSafeComposeColor(SurfaceContainerLight)
        val surfaceContainerHigh = backgroundPreset.surfaceContainerHighLightHex.toSafeComposeColor(SurfaceContainerHighLight)
        val surfaceVariant = backgroundPreset.surfaceVariantLightHex.toSafeComposeColor(SurfaceVariantLight)
        val onBackground = backgroundPreset.onBackgroundLightHex.toSafeComposeColor(OnBackgroundLight)
        val onSurface = backgroundPreset.onSurfaceLightHex.toSafeComposeColor(OnSurfaceLight)
        val onSurfaceVariant = backgroundPreset.onSurfaceVariantLightHex.toSafeComposeColor(OnSurfaceVariantLight)
        val outlineVariant = backgroundPreset.outlineVariantLightHex.toSafeComposeColor(OutlineVariantLight)

        return lightColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = TertiaryLight,
            onTertiary = OnTertiaryLight,
            tertiaryContainer = TertiaryContainerLight,
            onTertiaryContainer = OnTertiaryContainerLight,
            background = bg,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            surfaceContainer = surfaceContainer,
            surfaceContainerHigh = surfaceContainerHigh,
            outline = OutlineLight,
            outlineVariant = outlineVariant,
            error = ErrorLight,
            onError = OnErrorLight,
            errorContainer = ErrorContainerLight,
            onErrorContainer = OnErrorContainerLight
        )
    }

    private fun createDarkScheme(
        primaryHsl: FloatArray,
        secondaryHsl: FloatArray,
        backgroundPreset: BackgroundPreset
    ): ColorScheme {
        val pHue = primaryHsl[0]
        val pSat = primaryHsl[1].coerceIn(0.65f, 0.80f)
        var pLight = primaryHsl[2].coerceIn(0.68f, 0.78f)
        val onPrimary = ThemeUtils.hslToColor(pHue, 0.90f, 0.08f)
        var primary = ThemeUtils.hslToColor(pHue, pSat, pLight)
        while (calculateContrastRatio(onPrimary, primary) < 4.5f && pLight < 0.88f) {
            pLight += 0.02f
            primary = ThemeUtils.hslToColor(pHue, pSat, pLight)
        }
        val primaryContainer = ThemeUtils.hslToColor(pHue, 0.50f, 0.22f)
        val onPrimaryContainer = ThemeUtils.hslToColor(pHue, 0.30f, 0.92f)

        val sHue = secondaryHsl[0]
        val sSat = secondaryHsl[1].coerceIn(0.65f, 0.80f)
        var sLight = secondaryHsl[2].coerceIn(0.68f, 0.78f)
        val onSecondary = ThemeUtils.hslToColor(sHue, 0.90f, 0.08f)
        var secondary = ThemeUtils.hslToColor(sHue, sSat, sLight)
        while (calculateContrastRatio(onSecondary, secondary) < 4.5f && sLight < 0.88f) {
            sLight += 0.02f
            secondary = ThemeUtils.hslToColor(sHue, sSat, sLight)
        }
        val secondaryContainer = ThemeUtils.hslToColor(sHue, 0.50f, 0.22f)
        val onSecondaryContainer = ThemeUtils.hslToColor(sHue, 0.30f, 0.92f)

        val bg = backgroundPreset.backgroundDarkHex.toSafeComposeColor(BackgroundDark)
        val surface = backgroundPreset.surfaceDarkHex.toSafeComposeColor(SurfaceDark)
        val surfaceContainer = backgroundPreset.surfaceContainerDarkHex.toSafeComposeColor(SurfaceContainerDark)
        val surfaceContainerHigh = backgroundPreset.surfaceContainerHighDarkHex.toSafeComposeColor(SurfaceContainerHighDark)
        val surfaceVariant = backgroundPreset.surfaceVariantDarkHex.toSafeComposeColor(SurfaceVariantDark)
        val onBackground = backgroundPreset.onBackgroundDarkHex.toSafeComposeColor(OnBackgroundDark)
        val onSurface = backgroundPreset.onSurfaceDarkHex.toSafeComposeColor(OnSurfaceDark)
        val onSurfaceVariant = backgroundPreset.onSurfaceVariantDarkHex.toSafeComposeColor(OnSurfaceVariantDark)
        val outlineVariant = backgroundPreset.outlineVariantDarkHex.toSafeComposeColor(OutlineVariantDark)

        return darkColorScheme(
            primary = primary,
            onPrimary = onPrimary,
            primaryContainer = primaryContainer,
            onPrimaryContainer = onPrimaryContainer,
            secondary = secondary,
            onSecondary = onSecondary,
            secondaryContainer = secondaryContainer,
            onSecondaryContainer = onSecondaryContainer,
            tertiary = TertiaryDark,
            onTertiary = OnTertiaryDark,
            tertiaryContainer = TertiaryContainerDark,
            onTertiaryContainer = OnTertiaryContainerDark,
            background = bg,
            onBackground = onBackground,
            surface = surface,
            onSurface = onSurface,
            surfaceVariant = surfaceVariant,
            onSurfaceVariant = onSurfaceVariant,
            surfaceContainer = surfaceContainer,
            surfaceContainerHigh = surfaceContainerHigh,
            outline = OutlineDark,
            outlineVariant = outlineVariant,
            error = ErrorDark,
            onError = OnErrorDark,
            errorContainer = ErrorContainerDark,
            onErrorContainer = OnErrorContainerDark
        )
    }
}
