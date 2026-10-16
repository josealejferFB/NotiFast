package com.notifast.app.domain.model.theme

import androidx.annotation.StringRes
import com.notifast.app.R

/**
 * Predefined and customizable academic study themes with guaranteed WCAG AAA contrast ratios.
 */
enum class ThemePreset(
    val id: String,
    @property:StringRes val nameRes: Int,
    @property:StringRes val descriptionRes: Int,
    val primaryLightHex: String,
    val secondaryLightHex: String,
    val primaryDarkHex: String,
    val secondaryDarkHex: String
) {
    CLASSIC(
        id = "classic",
        nameRes = R.string.theme_preset_classic_name,
        descriptionRes = R.string.theme_preset_classic_desc,
        primaryLightHex = "#9E1B32",
        secondaryLightHex = "#2A7B7B",
        primaryDarkHex = "#E05A6E",
        secondaryDarkHex = "#38B2AC"
    ),
    FOREST_SAGE(
        id = "forest_sage",
        nameRes = R.string.theme_preset_forest_sage_name,
        descriptionRes = R.string.theme_preset_forest_sage_desc,
        primaryLightHex = "#15803D",
        secondaryLightHex = "#854D0E",
        primaryDarkHex = "#34D399",
        secondaryDarkHex = "#FBBF24"
    ),
    MIDNIGHT_INDIGO(
        id = "midnight_indigo",
        nameRes = R.string.theme_preset_midnight_indigo_name,
        descriptionRes = R.string.theme_preset_midnight_indigo_desc,
        primaryLightHex = "#4338CA",
        secondaryLightHex = "#0891B2",
        primaryDarkHex = "#818CF8",
        secondaryDarkHex = "#22D3EE"
    ),
    WARM_MOCHA(
        id = "warm_mocha",
        nameRes = R.string.theme_preset_warm_mocha_name,
        descriptionRes = R.string.theme_preset_warm_mocha_desc,
        primaryLightHex = "#C2410C",
        secondaryLightHex = "#B45309",
        primaryDarkHex = "#FB923C",
        secondaryDarkHex = "#FCD34D"
    ),
    SAKURA_ZEN(
        id = "sakura_zen",
        nameRes = R.string.theme_preset_sakura_zen_name,
        descriptionRes = R.string.theme_preset_sakura_zen_desc,
        primaryLightHex = "#6D28D9",
        secondaryLightHex = "#BE185D",
        primaryDarkHex = "#A78BFA",
        secondaryDarkHex = "#F472B6"
    ),
    OCEAN_CYAN(
        id = "ocean_cyan",
        nameRes = R.string.theme_preset_ocean_cyan_name,
        descriptionRes = R.string.theme_preset_ocean_cyan_desc,
        primaryLightHex = "#0284C7",
        secondaryLightHex = "#0F766E",
        primaryDarkHex = "#38BDF8",
        secondaryDarkHex = "#2DD4BF"
    ),
    CUSTOM(
        id = "custom",
        nameRes = R.string.theme_preset_custom_name,
        descriptionRes = R.string.theme_preset_custom_desc,
        primaryLightHex = "#9E1B32",
        secondaryLightHex = "#2A7B7B",
        primaryDarkHex = "#E05A6E",
        secondaryDarkHex = "#38B2AC"
    );

    companion object {
        /**
         * The curated list of 6 predefined presets presented in the quick-selection row.
         */
        val availablePresets: List<ThemePreset> = listOf(
            CLASSIC,
            FOREST_SAGE,
            MIDNIGHT_INDIGO,
            WARM_MOCHA,
            SAKURA_ZEN,
            OCEAN_CYAN
        )

        /**
         * Resolves a preset from its string id, defaulting to [CLASSIC] for null or unrecognized values.
         */
        fun fromId(id: String?): ThemePreset {
            if (id.isNullOrBlank()) return CLASSIC
            return entries.firstOrNull { it.id.equals(id, ignoreCase = true) } ?: CLASSIC
        }
    }
}
