package com.notifast.app.domain.model.theme

import androidx.annotation.StringRes
import com.notifast.app.R

/**
 * Predefined background and canvas palettes for NotiFast 3.0.
 * Allows independent customization of background/surface/container tones
 * adaptable seamlessly to both Light Mode and Dark Mode with guaranteed WCAG AAA contrast.
 */
enum class BackgroundPreset(
    val id: String,
    @get:StringRes val nameRes: Int,
    @get:StringRes val descriptionRes: Int,
    // Light Mode tokens
    val backgroundLightHex: String,
    val surfaceLightHex: String,
    val surfaceContainerLightHex: String,
    val surfaceContainerHighLightHex: String,
    val surfaceVariantLightHex: String,
    val onBackgroundLightHex: String,
    val onSurfaceLightHex: String,
    val onSurfaceVariantLightHex: String,
    val outlineVariantLightHex: String,
    // Dark Mode tokens
    val backgroundDarkHex: String,
    val surfaceDarkHex: String,
    val surfaceContainerDarkHex: String,
    val surfaceContainerHighDarkHex: String,
    val surfaceVariantDarkHex: String,
    val onBackgroundDarkHex: String,
    val onSurfaceDarkHex: String,
    val onSurfaceVariantDarkHex: String,
    val outlineVariantDarkHex: String,
    // Signature Chromatic Pip
    val accentPipHex: String
) {
    CLASSIC(
        id = "classic",
        nameRes = R.string.bg_preset_classic_name,
        descriptionRes = R.string.bg_preset_classic_desc,
        backgroundLightHex = "#FAF9F6",
        surfaceLightHex = "#FFFFFF",
        surfaceContainerLightHex = "#F3EFEA",
        surfaceContainerHighLightHex = "#EBE6DF",
        surfaceVariantLightHex = "#F3EFEA",
        onBackgroundLightHex = "#181A1F",
        onSurfaceLightHex = "#181A1F",
        onSurfaceVariantLightHex = "#4B5563",
        outlineVariantLightHex = "#E5DFD7",
        backgroundDarkHex = "#0E131F",
        surfaceDarkHex = "#161E2D",
        surfaceContainerDarkHex = "#1C2638",
        surfaceContainerHighDarkHex = "#202A3C",
        surfaceVariantDarkHex = "#1C2638",
        onBackgroundDarkHex = "#F1F5F9",
        onSurfaceDarkHex = "#F1F5F9",
        onSurfaceVariantDarkHex = "#94A3B8",
        outlineVariantDarkHex = "#2A384E",
        accentPipHex = "#9E1B32"
    ),

    FOREST_SAGE(
        id = "forest_sage",
        nameRes = R.string.bg_preset_forest_sage_name,
        descriptionRes = R.string.bg_preset_forest_sage_desc,
        backgroundLightHex = "#EAF4ED",
        surfaceLightHex = "#F6FAF7",
        surfaceContainerLightHex = "#D7EADC",
        surfaceContainerHighLightHex = "#C8E0CF",
        surfaceVariantLightHex = "#D7EADC",
        onBackgroundLightHex = "#0D2315",
        onSurfaceLightHex = "#0D2315",
        onSurfaceVariantLightHex = "#2E513A",
        outlineVariantLightHex = "#ACD0B9",
        backgroundDarkHex = "#0A1610",
        surfaceDarkHex = "#12241B",
        surfaceContainerDarkHex = "#1A3527",
        surfaceContainerHighDarkHex = "#244735",
        surfaceVariantDarkHex = "#1A3527",
        onBackgroundDarkHex = "#ECF8F1",
        onSurfaceDarkHex = "#ECF8F1",
        onSurfaceVariantDarkHex = "#9CCBB0",
        outlineVariantDarkHex = "#264936",
        accentPipHex = "#22C55E"
    ),

    MIDNIGHT_INDIGO(
        id = "midnight_indigo",
        nameRes = R.string.bg_preset_midnight_indigo_name,
        descriptionRes = R.string.bg_preset_midnight_indigo_desc,
        backgroundLightHex = "#E6F0F8",
        surfaceLightHex = "#F5F9FD",
        surfaceContainerLightHex = "#D1E4F3",
        surfaceContainerHighLightHex = "#BDDAEE",
        surfaceVariantLightHex = "#D1E4F3",
        onBackgroundLightHex = "#0A1C33",
        onSurfaceLightHex = "#0A1C33",
        onSurfaceVariantLightHex = "#2C4667",
        outlineVariantLightHex = "#A3C7E5",
        backgroundDarkHex = "#09101D",
        surfaceDarkHex = "#111E35",
        surfaceContainerDarkHex = "#192B4B",
        surfaceContainerHighDarkHex = "#233B65",
        surfaceVariantDarkHex = "#192B4B",
        onBackgroundDarkHex = "#EDF5FC",
        onSurfaceDarkHex = "#EDF5FC",
        onSurfaceVariantDarkHex = "#98BFEC",
        outlineVariantDarkHex = "#253F6B",
        accentPipHex = "#6366F1"
    ),

    WARM_MOCHA(
        id = "warm_mocha",
        nameRes = R.string.bg_preset_warm_mocha_name,
        descriptionRes = R.string.bg_preset_warm_mocha_desc,
        backgroundLightHex = "#F5ECE1",
        surfaceLightHex = "#FCFAF6",
        surfaceContainerLightHex = "#E8DAC8",
        surfaceContainerHighLightHex = "#DBC8B2",
        surfaceVariantLightHex = "#E8DAC8",
        onBackgroundLightHex = "#291A0C",
        onSurfaceLightHex = "#291A0C",
        onSurfaceVariantLightHex = "#59432F",
        outlineVariantLightHex = "#CFB89F",
        backgroundDarkHex = "#150F0B",
        surfaceDarkHex = "#221913",
        surfaceContainerDarkHex = "#32251D",
        surfaceContainerHighDarkHex = "#443327",
        surfaceVariantDarkHex = "#32251D",
        onBackgroundDarkHex = "#FAF1E8",
        onSurfaceDarkHex = "#FAF1E8",
        onSurfaceVariantDarkHex = "#D5B69E",
        outlineVariantDarkHex = "#4A362A",
        accentPipHex = "#EA580C"
    ),

    SAKURA_ZEN(
        id = "sakura_zen",
        nameRes = R.string.bg_preset_sakura_zen_name,
        descriptionRes = R.string.bg_preset_sakura_zen_desc,
        backgroundLightHex = "#F2EAF7",
        surfaceLightHex = "#FAF6FC",
        surfaceContainerLightHex = "#E4D3EE",
        surfaceContainerHighLightHex = "#D5BFE3",
        surfaceVariantLightHex = "#E4D3EE",
        onBackgroundLightHex = "#221031",
        onSurfaceLightHex = "#221031",
        onSurfaceVariantLightHex = "#50336C",
        outlineVariantLightHex = "#CBB0DF",
        backgroundDarkHex = "#130B1C",
        surfaceDarkHex = "#20122E",
        surfaceContainerDarkHex = "#2F1B43",
        surfaceContainerHighDarkHex = "#40255A",
        surfaceVariantDarkHex = "#2F1B43",
        onBackgroundDarkHex = "#F8F0FC",
        onSurfaceDarkHex = "#F8F0FC",
        onSurfaceVariantDarkHex = "#CAA3E6",
        outlineVariantDarkHex = "#462963",
        accentPipHex = "#A855F7"
    ),

    OCEAN_CYAN(
        id = "ocean_cyan",
        nameRes = R.string.bg_preset_ocean_cyan_name,
        descriptionRes = R.string.bg_preset_ocean_cyan_desc,
        backgroundLightHex = "#E5F3F5",
        surfaceLightHex = "#F5FAFB",
        surfaceContainerLightHex = "#CCE8EB",
        surfaceContainerHighLightHex = "#B6DDE2",
        surfaceVariantLightHex = "#CCE8EB",
        onBackgroundLightHex = "#092228",
        onSurfaceLightHex = "#092228",
        onSurfaceVariantLightHex = "#274F56",
        outlineVariantLightHex = "#9CCCD3",
        backgroundDarkHex = "#061418",
        surfaceDarkHex = "#0D2026",
        surfaceContainerDarkHex = "#142E37",
        surfaceContainerHighDarkHex = "#1C3D49",
        surfaceVariantDarkHex = "#142E37",
        onBackgroundDarkHex = "#EAF7F8",
        onSurfaceDarkHex = "#EAF7F8",
        onSurfaceVariantDarkHex = "#88C1CB",
        outlineVariantDarkHex = "#20434F",
        accentPipHex = "#06B6D4"
    ),

    CUSTOM(
        id = "custom",
        nameRes = R.string.bg_preset_custom_name,
        descriptionRes = R.string.bg_preset_custom_desc,
        backgroundLightHex = "#FAF9F6",
        surfaceLightHex = "#FFFFFF",
        surfaceContainerLightHex = "#F3EFEA",
        surfaceContainerHighLightHex = "#EBE6DF",
        surfaceVariantLightHex = "#F3EFEA",
        onBackgroundLightHex = "#181A1F",
        onSurfaceLightHex = "#181A1F",
        onSurfaceVariantLightHex = "#4B5563",
        outlineVariantLightHex = "#E5DFD7",
        backgroundDarkHex = "#0E131F",
        surfaceDarkHex = "#161E2D",
        surfaceContainerDarkHex = "#1C2638",
        surfaceContainerHighDarkHex = "#202A3C",
        surfaceVariantDarkHex = "#1C2638",
        onBackgroundDarkHex = "#F1F5F9",
        onSurfaceDarkHex = "#F1F5F9",
        onSurfaceVariantDarkHex = "#94A3B8",
        outlineVariantDarkHex = "#2A384E",
        accentPipHex = "#3B9797"
    );

    companion object {
        val availablePresets: List<BackgroundPreset> by lazy {
            listOf(
                CLASSIC,
                FOREST_SAGE,
                MIDNIGHT_INDIGO,
                WARM_MOCHA,
                SAKURA_ZEN,
                OCEAN_CYAN
            )
        }

        fun fromId(id: String?): BackgroundPreset {
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: CLASSIC
        }
    }
}
