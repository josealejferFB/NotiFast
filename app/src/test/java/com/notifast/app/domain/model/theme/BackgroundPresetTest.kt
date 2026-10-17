package com.notifast.app.domain.model.theme

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class BackgroundPresetTest {

    @Test
    fun `fromId returns correct preset for valid ids`() {
        assertEquals(BackgroundPreset.CLASSIC, BackgroundPreset.fromId("classic"))
        assertEquals(BackgroundPreset.FOREST_SAGE, BackgroundPreset.fromId("forest_sage"))
        assertEquals(BackgroundPreset.MIDNIGHT_INDIGO, BackgroundPreset.fromId("midnight_indigo"))
        assertEquals(BackgroundPreset.WARM_MOCHA, BackgroundPreset.fromId("warm_mocha"))
        assertEquals(BackgroundPreset.SAKURA_ZEN, BackgroundPreset.fromId("sakura_zen"))
        assertEquals(BackgroundPreset.OCEAN_CYAN, BackgroundPreset.fromId("ocean_cyan"))
    }

    @Test
    fun `fromId falls back to CLASSIC for null empty or unknown id`() {
        assertEquals(BackgroundPreset.CLASSIC, BackgroundPreset.fromId(null))
        assertEquals(BackgroundPreset.CLASSIC, BackgroundPreset.fromId(""))
        assertEquals(BackgroundPreset.CLASSIC, BackgroundPreset.fromId("non_existent_preset"))
    }

    @Test
    fun `availablePresets contains the 6 academic background presets in order`() {
        val presets = BackgroundPreset.availablePresets
        assertEquals(6, presets.size)
        assertEquals(BackgroundPreset.CLASSIC, presets[0])
        assertEquals(BackgroundPreset.FOREST_SAGE, presets[1])
        assertEquals(BackgroundPreset.MIDNIGHT_INDIGO, presets[2])
        assertEquals(BackgroundPreset.WARM_MOCHA, presets[3])
        assertEquals(BackgroundPreset.SAKURA_ZEN, presets[4])
        assertEquals(BackgroundPreset.OCEAN_CYAN, presets[5])
    }

    @Test
    fun `all presets have valid 7-character hex color codes for background and surfaces`() {
        val hexRegex = Regex("^#[0-9A-Fa-f]{6}$")
        for (preset in BackgroundPreset.entries) {
            assertTrue("backgroundLightHex for ${preset.name} should match #RRGGBB", hexRegex.matches(preset.backgroundLightHex))
            assertTrue("surfaceLightHex for ${preset.name} should match #RRGGBB", hexRegex.matches(preset.surfaceLightHex))
            assertTrue("surfaceContainerLightHex for ${preset.name} should match #RRGGBB", hexRegex.matches(preset.surfaceContainerLightHex))
            assertTrue("surfaceContainerHighLightHex for ${preset.name} should match #RRGGBB", hexRegex.matches(preset.surfaceContainerHighLightHex))
            assertTrue("surfaceVariantLightHex for ${preset.name} should match #RRGGBB", hexRegex.matches(preset.surfaceVariantLightHex))
            assertTrue("onBackgroundLightHex for ${preset.name} should match #RRGGBB", hexRegex.matches(preset.onBackgroundLightHex))
            assertTrue("onSurfaceLightHex for ${preset.name} should match #RRGGBB", hexRegex.matches(preset.onSurfaceLightHex))
            assertTrue("onSurfaceVariantLightHex for ${preset.name} should match #RRGGBB", hexRegex.matches(preset.onSurfaceVariantLightHex))
            assertTrue("outlineVariantLightHex for ${preset.name} should match #RRGGBB", hexRegex.matches(preset.outlineVariantLightHex))

            assertTrue("backgroundDarkHex for ${preset.name} should match #RRGGBB", hexRegex.matches(preset.backgroundDarkHex))
            assertTrue("surfaceDarkHex for ${preset.name} should match #RRGGBB", hexRegex.matches(preset.surfaceDarkHex))
            assertTrue("surfaceContainerDarkHex for ${preset.name} should match #RRGGBB", hexRegex.matches(preset.surfaceContainerDarkHex))
            assertTrue("surfaceContainerHighDarkHex for ${preset.name} should match #RRGGBB", hexRegex.matches(preset.surfaceContainerHighDarkHex))
            assertTrue("surfaceVariantDarkHex for ${preset.name} should match #RRGGBB", hexRegex.matches(preset.surfaceVariantDarkHex))
            assertTrue("onBackgroundDarkHex for ${preset.name} should match #RRGGBB", hexRegex.matches(preset.onBackgroundDarkHex))
            assertTrue("onSurfaceDarkHex for ${preset.name} should match #RRGGBB", hexRegex.matches(preset.onSurfaceDarkHex))
            assertTrue("onSurfaceVariantDarkHex for ${preset.name} should match #RRGGBB", hexRegex.matches(preset.onSurfaceVariantDarkHex))
            assertTrue("outlineVariantDarkHex for ${preset.name} should match #RRGGBB", hexRegex.matches(preset.outlineVariantDarkHex))

            assertTrue("accentPipHex for ${preset.name} should match #RRGGBB", hexRegex.matches(preset.accentPipHex))
        }
    }

    @Test
    fun `all presets have valid string resource references`() {
        for (preset in BackgroundPreset.entries) {
            assertTrue("Name resource for ${preset.name} should be non-zero", preset.nameRes != 0)
            assertTrue("Description resource for ${preset.name} should be non-zero", preset.descriptionRes != 0)
        }
    }
}
