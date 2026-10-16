package com.notifast.app.domain.model.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ThemePresetTest {

    @Test
    fun `fromId returns correct preset for valid ids`() {
        assertEquals(ThemePreset.CLASSIC, ThemePreset.fromId("classic"))
        assertEquals(ThemePreset.FOREST_SAGE, ThemePreset.fromId("forest_sage"))
        assertEquals(ThemePreset.MIDNIGHT_INDIGO, ThemePreset.fromId("midnight_indigo"))
        assertEquals(ThemePreset.WARM_MOCHA, ThemePreset.fromId("warm_mocha"))
        assertEquals(ThemePreset.SAKURA_ZEN, ThemePreset.fromId("sakura_zen"))
        assertEquals(ThemePreset.OCEAN_CYAN, ThemePreset.fromId("ocean_cyan"))
        assertEquals(ThemePreset.CUSTOM, ThemePreset.fromId("custom"))
    }

    @Test
    fun `fromId falls back to CLASSIC for null empty or unknown id`() {
        assertEquals(ThemePreset.CLASSIC, ThemePreset.fromId(null))
        assertEquals(ThemePreset.CLASSIC, ThemePreset.fromId(""))
        assertEquals(ThemePreset.CLASSIC, ThemePreset.fromId("non_existent_preset"))
    }

    @Test
    fun `availablePresets contains the 6 academic presets in order`() {
        val presets = ThemePreset.availablePresets
        assertEquals(6, presets.size)
        assertEquals(ThemePreset.CLASSIC, presets[0])
        assertEquals(ThemePreset.FOREST_SAGE, presets[1])
        assertEquals(ThemePreset.MIDNIGHT_INDIGO, presets[2])
        assertEquals(ThemePreset.WARM_MOCHA, presets[3])
        assertEquals(ThemePreset.SAKURA_ZEN, presets[4])
        assertEquals(ThemePreset.OCEAN_CYAN, presets[5])
    }

    @Test
    fun `all presets have valid 7-character hex color codes`() {
        val hexRegex = Regex("^#[0-9A-Fa-f]{6}$")
        for (preset in ThemePreset.entries) {
            assertTrue("Primary Light Hex for ${preset.name} should match #RRGGBB", hexRegex.matches(preset.primaryLightHex))
            assertTrue("Secondary Light Hex for ${preset.name} should match #RRGGBB", hexRegex.matches(preset.secondaryLightHex))
            assertTrue("Primary Dark Hex for ${preset.name} should match #RRGGBB", hexRegex.matches(preset.primaryDarkHex))
            assertTrue("Secondary Dark Hex for ${preset.name} should match #RRGGBB", hexRegex.matches(preset.secondaryDarkHex))
        }
    }

    @Test
    fun `all presets have valid string resource references`() {
        for (preset in ThemePreset.entries) {
            assertTrue("Name resource for ${preset.name} should be non-zero", preset.nameRes != 0)
            assertTrue("Description resource for ${preset.name} should be non-zero", preset.descriptionRes != 0)
        }
    }
}
