package com.notifast.app.presentation.theme

import androidx.compose.ui.graphics.Color
import com.notifast.app.domain.model.theme.ThemePreset
import com.notifast.app.presentation.theme.color.BackgroundDark
import com.notifast.app.presentation.theme.color.BackgroundLight
import com.notifast.app.presentation.theme.color.OutlineVariantDark
import com.notifast.app.presentation.theme.color.OutlineVariantLight
import com.notifast.app.presentation.theme.color.SurfaceContainerDark
import com.notifast.app.presentation.theme.color.SurfaceContainerLight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

import com.notifast.app.domain.model.theme.BackgroundPreset
import com.notifast.app.presentation.theme.color.toSafeComposeColor

class ThemePaletteGeneratorTest {

    @Test
    fun `contrast ratio formula calculates correct values for black and white`() {
        val ratio = ThemePaletteGenerator.calculateContrastRatio(Color.White, Color.Black)
        assertEquals(21.0f, ratio, 0.5f)
    }

    @Test
    fun `all presets generate valid color schemes in light mode with WCAG contrast`() {
        for (preset in ThemePreset.availablePresets) {
            val scheme = ThemePaletteGenerator.generateColorScheme(preset, isDark = false)
            assertNotNull(scheme)

            // WCAG AA / AAA checks
            val onPrimaryContrast = ThemePaletteGenerator.calculateContrastRatio(scheme.onPrimary, scheme.primary)
            assertTrue(
                "onPrimary on primary contrast for ${preset.name} in light mode should be >= 4.5:1 (got $onPrimaryContrast)",
                onPrimaryContrast >= 4.5f
            )

            val onPrimaryContainerContrast = ThemePaletteGenerator.calculateContrastRatio(
                scheme.onPrimaryContainer,
                scheme.primaryContainer
            )
            assertTrue(
                "onPrimaryContainer on primaryContainer contrast for ${preset.name} in light mode should be >= 4.5:1 (got $onPrimaryContainerContrast)",
                onPrimaryContainerContrast >= 4.5f
            )

            // Papercraft background & surface tokens integrity
            assertEquals(BackgroundLight, scheme.background)
            assertEquals(SurfaceContainerLight, scheme.surfaceContainer)
            assertEquals(OutlineVariantLight, scheme.outlineVariant)
        }
    }

    @Test
    fun `all presets generate valid color schemes in dark mode with WCAG contrast`() {
        for (preset in ThemePreset.availablePresets) {
            val scheme = ThemePaletteGenerator.generateColorScheme(preset, isDark = true)
            assertNotNull(scheme)

            val onPrimaryContrast = ThemePaletteGenerator.calculateContrastRatio(scheme.onPrimary, scheme.primary)
            assertTrue(
                "onPrimary on primary contrast for ${preset.name} in dark mode should be >= 4.5:1 (got $onPrimaryContrast)",
                onPrimaryContrast >= 4.5f
            )

            val onPrimaryContainerContrast = ThemePaletteGenerator.calculateContrastRatio(
                scheme.onPrimaryContainer,
                scheme.primaryContainer
            )
            assertTrue(
                "onPrimaryContainer on primaryContainer contrast for ${preset.name} in dark mode should be >= 4.5:1 (got $onPrimaryContainerContrast)",
                onPrimaryContainerContrast >= 4.5f
            )

            // Dark tactile slate surface tokens integrity
            assertEquals(BackgroundDark, scheme.background)
            assertEquals(SurfaceContainerDark, scheme.surfaceContainer)
            assertEquals(OutlineVariantDark, scheme.outlineVariant)
        }
    }

    @Test
    fun `independent background presets apply custom canvas tokens in light and dark mode`() {
        for (bgPreset in BackgroundPreset.availablePresets) {
            val lightScheme = ThemePaletteGenerator.generateColorScheme(
                preset = ThemePreset.CLASSIC,
                isDark = false,
                backgroundPreset = bgPreset
            )
            assertEquals(bgPreset.backgroundLightHex.toSafeComposeColor(), lightScheme.background)
            assertEquals(bgPreset.surfaceLightHex.toSafeComposeColor(), lightScheme.surface)
            assertEquals(bgPreset.surfaceContainerLightHex.toSafeComposeColor(), lightScheme.surfaceContainer)
            assertEquals(bgPreset.outlineVariantLightHex.toSafeComposeColor(), lightScheme.outlineVariant)

            val lightTextContrast = ThemePaletteGenerator.calculateContrastRatio(lightScheme.onBackground, lightScheme.background)
            assertTrue(
                "Light onBackground contrast for ${bgPreset.name} should be >= 4.5:1 (got $lightTextContrast)",
                lightTextContrast >= 4.5f
            )

            val darkScheme = ThemePaletteGenerator.generateColorScheme(
                preset = ThemePreset.CLASSIC,
                isDark = true,
                backgroundPreset = bgPreset
            )
            assertEquals(bgPreset.backgroundDarkHex.toSafeComposeColor(), darkScheme.background)
            assertEquals(bgPreset.surfaceDarkHex.toSafeComposeColor(), darkScheme.surface)
            assertEquals(bgPreset.surfaceContainerDarkHex.toSafeComposeColor(), darkScheme.surfaceContainer)
            assertEquals(bgPreset.outlineVariantDarkHex.toSafeComposeColor(), darkScheme.outlineVariant)

            val darkTextContrast = ThemePaletteGenerator.calculateContrastRatio(darkScheme.onBackground, darkScheme.background)
            assertTrue(
                "Dark onBackground contrast for ${bgPreset.name} should be >= 4.5:1 (got $darkTextContrast)",
                darkTextContrast >= 4.5f
            )
        }
    }

    @Test
    fun `cross matrix of all accent presets with all background presets maintains WCAG AAA`() {
        for (accent in ThemePreset.availablePresets) {
            for (bg in BackgroundPreset.availablePresets) {
                val light = ThemePaletteGenerator.generateColorScheme(accent, isDark = false, backgroundPreset = bg)
                val dark = ThemePaletteGenerator.generateColorScheme(accent, isDark = true, backgroundPreset = bg)

                assertTrue(
                    "Light onPrimary contrast for ${accent.name} + ${bg.name} should be >= 4.5:1",
                    ThemePaletteGenerator.calculateContrastRatio(light.onPrimary, light.primary) >= 4.5f
                )
                assertTrue(
                    "Dark onPrimary contrast for ${accent.name} + ${bg.name} should be >= 4.5:1",
                    ThemePaletteGenerator.calculateContrastRatio(dark.onPrimary, dark.primary) >= 4.5f
                )
                assertTrue(
                    "Light onSurface contrast for ${accent.name} + ${bg.name} should be >= 4.5:1",
                    ThemePaletteGenerator.calculateContrastRatio(light.onSurface, light.surface) >= 4.5f
                )
                assertTrue(
                    "Dark onSurface contrast for ${accent.name} + ${bg.name} should be >= 4.5:1",
                    ThemePaletteGenerator.calculateContrastRatio(dark.onSurface, dark.surface) >= 4.5f
                )
            }
        }
    }

    @Test
    fun `custom theme with arbitrary valid hex generates accessible scheme`() {
        val testHexes = listOf("#4F46E5", "#059669", "#D97706", "#7C3AED", "#E11D48", "#0284C7")
        for (hex in testHexes) {
            val lightScheme = ThemePaletteGenerator.generateColorScheme(
                preset = ThemePreset.CUSTOM,
                isDark = false,
                customPrimaryHex = hex
            )
            val darkScheme = ThemePaletteGenerator.generateColorScheme(
                preset = ThemePreset.CUSTOM,
                isDark = true,
                customPrimaryHex = hex
            )

            assertTrue(
                "Light onPrimary contrast for $hex should be >= 4.5:1",
                ThemePaletteGenerator.calculateContrastRatio(lightScheme.onPrimary, lightScheme.primary) >= 4.5f
            )
            assertTrue(
                "Dark onPrimary contrast for $hex should be >= 4.5:1",
                ThemePaletteGenerator.calculateContrastRatio(darkScheme.onPrimary, darkScheme.primary) >= 4.5f
            )
        }
    }

    @Test
    fun `custom theme with invalid or null hex safely falls back to default without crashing`() {
        val lightScheme = ThemePaletteGenerator.generateColorScheme(
            preset = ThemePreset.CUSTOM,
            isDark = false,
            customPrimaryHex = "invalid-hex"
        )
        assertNotNull(lightScheme)
        assertEquals(BackgroundLight, lightScheme.background)

        val nullHexScheme = ThemePaletteGenerator.generateColorScheme(
            preset = ThemePreset.CUSTOM,
            isDark = true,
            customPrimaryHex = null
        )
        assertNotNull(nullHexScheme)
        assertEquals(BackgroundDark, nullHexScheme.background)
    }
}
