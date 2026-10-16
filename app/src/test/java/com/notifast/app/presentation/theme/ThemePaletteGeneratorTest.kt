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
