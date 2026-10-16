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
    val surfaceVariantLightHex: String,
    val onBackgroundLightHex: String,
    val onSurfaceLightHex: String,
    val outlineVariantLightHex: String,
    // Dark Mode tokens
    val backgroundDarkHex: String,
    val surfaceDarkHex: String,
    val surfaceContainerDarkHex: String,
    val surfaceVariantDarkHex: String,
    val onBackgroundDarkHex: String,
    val onSurfaceDarkHex: String,
    val outlineVariantDarkHex: String
) {
    CLASSIC(
        id = "classic",
        nameRes = R.string.bg_preset_classic_name,
        descriptionRes = R.string.bg_preset_classic_desc,
        backgroundLightHex = "#FAF9F6",
        surfaceLightHex = "#FFFFFF",
        surfaceContainerLightHex = "#F3EFEA",
        surfaceVariantLightHex = "#F3EFEA",
        onBackgroundLightHex = "#181A1F",
        onSurfaceLightHex = "#181A1F",
        outlineVariantLightHex = "#E5DFD7",
        backgroundDarkHex = "#0E131F",
        surfaceDarkHex = "#161E2D",
        surfaceContainerDarkHex = "#1C2638",
        surfaceVariantDarkHex = "#1C2638",
        onBackgroundDarkHex = "#F1F5F9",
        onSurfaceDarkHex = "#F1F5F9",
        outlineVariantDarkHex = "#2A384E"
    ),

    FOREST_SAGE(
        id = "forest_sage",
        nameRes = R.string.bg_preset_forest_sage_name,
        descriptionRes = R.string.bg_preset_forest_sage_desc,
        backgroundLightHex = "#F4F8F5",
        surfaceLightHex = "#FAFDFB",
        surfaceContainerLightHex = "#EAF2EC",
        surfaceVariantLightHex = "#DCE8DF",
        onBackgroundLightHex = "#112117",
        onSurfaceLightHex = "#112117",
        outlineVariantLightHex = "#D2E0D6",
        backgroundDarkHex = "#0C140F",
        surfaceDarkHex = "#121D17",
        surfaceContainerDarkHex = "#1A2820",
        surfaceVariantDarkHex = "#24362C",
        onBackgroundDarkHex = "#ECF5EE",
        onSurfaceDarkHex = "#ECF5EE",
        outlineVariantDarkHex = "#2B3E33"
    ),

    MIDNIGHT_INDIGO(
        id = "midnight_indigo",
        nameRes = R.string.bg_preset_midnight_indigo_name,
        descriptionRes = R.string.bg_preset_midnight_indigo_desc,
        backgroundLightHex = "#F2F6FA",
        surfaceLightHex = "#F9FBFD",
        surfaceContainerLightHex = "#E5EEF6",
        surfaceVariantLightHex = "#D6E3F0",
        onBackgroundLightHex = "#0C1726",
        onSurfaceLightHex = "#0C1726",
        outlineVariantLightHex = "#CADBEA",
        backgroundDarkHex = "#0A0E1A",
        surfaceDarkHex = "#101626",
        surfaceContainerDarkHex = "#192036",
        surfaceVariantDarkHex = "#242E4B",
        onBackgroundDarkHex = "#EDF2FC",
        onSurfaceDarkHex = "#EDF2FC",
        outlineVariantDarkHex = "#2B3758"
    ),

    WARM_MOCHA(
        id = "warm_mocha",
        nameRes = R.string.bg_preset_warm_mocha_name,
        descriptionRes = R.string.bg_preset_warm_mocha_desc,
        backgroundLightHex = "#FAF6F0",
        surfaceLightHex = "#FDFBFA",
        surfaceContainerLightHex = "#F2EAE0",
        surfaceVariantLightHex = "#E7DCCE",
        onBackgroundLightHex = "#261A0E",
        onSurfaceLightHex = "#261A0E",
        outlineVariantLightHex = "#DCCEC0",
        backgroundDarkHex = "#14100D",
        surfaceDarkHex = "#1B1612",
        surfaceContainerDarkHex = "#26201B",
        surfaceVariantDarkHex = "#342D26",
        onBackgroundDarkHex = "#F8F2EB",
        onSurfaceDarkHex = "#F8F2EB",
        outlineVariantDarkHex = "#3F362F"
    ),

    SAKURA_ZEN(
        id = "sakura_zen",
        nameRes = R.string.bg_preset_sakura_zen_name,
        descriptionRes = R.string.bg_preset_sakura_zen_desc,
        backgroundLightHex = "#F7F4FA",
        surfaceLightHex = "#FCFAFD",
        surfaceContainerLightHex = "#EFE9F5",
        surfaceVariantLightHex = "#E2D9EC",
        onBackgroundLightHex = "#1D1226",
        onSurfaceLightHex = "#1D1226",
        outlineVariantLightHex = "#D7CBE3",
        backgroundDarkHex = "#110C17",
        surfaceDarkHex = "#181220",
        surfaceContainerDarkHex = "#221B2C",
        surfaceVariantDarkHex = "#30273D",
        onBackgroundDarkHex = "#F4EDF9",
        onSurfaceDarkHex = "#F4EDF9",
        outlineVariantDarkHex = "#3A3049"
    ),

    OCEAN_CYAN(
        id = "ocean_cyan",
        nameRes = R.string.bg_preset_ocean_cyan_name,
        descriptionRes = R.string.bg_preset_ocean_cyan_desc,
        backgroundLightHex = "#F1F7F8",
        surfaceLightHex = "#F8FCFC",
        surfaceContainerLightHex = "#E3EFF1",
        surfaceVariantLightHex = "#D2E4E7",
        onBackgroundLightHex = "#0B1E22",
        onSurfaceLightHex = "#0B1E22",
        outlineVariantLightHex = "#C5DCE0",
        backgroundDarkHex = "#081216",
        surfaceDarkHex = "#0E1B20",
        surfaceContainerDarkHex = "#16262D",
        surfaceVariantDarkHex = "#20343D",
        onBackgroundDarkHex = "#EAF4F6",
        onSurfaceDarkHex = "#EAF4F6",
        outlineVariantDarkHex = "#273D47"
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
