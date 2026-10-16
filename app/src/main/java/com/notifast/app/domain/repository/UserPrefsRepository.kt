package com.notifast.app.domain.repository

import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for managing local student preferences via DataStore.
 */
interface UserPrefsRepository {
    /**
     * Flow emitting true for dark mode, false for light mode, or null to follow system theme.
     */
    val isDarkModeFlow: Flow<Boolean?>

    /**
     * Sets dark mode preference. Pass null to follow system theme.
     */
    suspend fun setDarkMode(enabled: Boolean?): Result<Unit>

    /**
     * Flow emitting current theme accent: "aqua", "ruby", "indigo", "terracotta", "sage".
     */
    val themeAccentFlow: Flow<String>

    /**
     * Sets theme accent preference.
     */
    suspend fun setThemeAccent(accent: String): Result<Unit>

    /**
     * Flow emitting current active theme preset ID (e.g. "classic", "forest_sage", "custom").
     */
    val themePresetIdFlow: Flow<String>

    /**
     * Sets active theme preset ID preference.
     */
    suspend fun setThemePresetId(presetId: String): Result<Unit>

    /**
     * Flow emitting current active background preset ID (e.g. "classic", "forest_sage", "warm_mocha").
     */
    val backgroundPresetIdFlow: Flow<String>

    /**
     * Sets active background preset ID preference.
     */
    suspend fun setBackgroundPresetId(presetId: String): Result<Unit>

    /**
     * Flow emitting custom primary color hex code (e.g. "#4F46E5"), or null if using presets.
     */
    val customPrimaryHexFlow: Flow<String?>

    /**
     * Sets custom primary color hex preference. Pass null to clear.
     */
    suspend fun setCustomPrimaryHex(hex: String?): Result<Unit>

    /**
     * Flow emitting whether tactile haptic feedback is enabled.
     */
    val hapticsEnabledFlow: Flow<Boolean>

    /**
     * Sets tactile haptic feedback preference.
     */
    suspend fun setHapticsEnabled(enabled: Boolean): Result<Unit>

    /**
     * Flow emitting whether class reminder alerts are enabled.
     */
    val classAlertsEnabledFlow: Flow<Boolean>

    /**
     * Sets whether class reminder alerts are enabled.
     */
    suspend fun setClassAlertsEnabled(enabled: Boolean): Result<Unit>

    /**
     * Flow emitting minutes before class to trigger notification (e.g. 10, 15, 30).
     */
    val classAlertMinutesFlow: Flow<Int>

    /**
     * Sets minutes before class for alert notification.
     */
    suspend fun setClassAlertMinutes(minutes: Int): Result<Unit>

    /**
     * Flow emitting whether evaluation reminder alerts are enabled.
     */
    val evalAlertsEnabledFlow: Flow<Boolean>

    /**
     * Sets whether evaluation reminder alerts are enabled.
     */
    suspend fun setEvalAlertsEnabled(enabled: Boolean): Result<Unit>

    /**
     * Flow emitting days before evaluation to trigger notification (e.g. 1, 2, 3).
     */
    val evalAlertDaysFlow: Flow<Int>

    /**
     * Sets days before evaluation for alert notification.
     */
    suspend fun setEvalAlertDays(days: Int): Result<Unit>

    /**
     * Flow emitting whether daily 07:00 AM morning summary notification is enabled.
     */
    val dailySummaryEnabledFlow: Flow<Boolean>

    /**
     * Sets daily morning summary notification preference.
     */
    suspend fun setDailySummaryEnabled(enabled: Boolean): Result<Unit>

    /**
     * Flow emitting the default grading scale format (e.g., "0-20", "0-10", "0-100").
     */
    val defaultGradeScaleFlow: Flow<String>

    /**
     * Sets the default grading scale format.
     */
    suspend fun setDefaultGradeScale(scale: String): Result<Unit>

    /**
     * Flow emitting the minimum passing grade threshold (e.g. 11.0f for 0-20, 6.0f for 0-10).
     */
    val minPassingGradeFlow: Flow<Float>

    /**
     * Sets the minimum passing grade threshold.
     */
    suspend fun setMinPassingGrade(grade: Float): Result<Unit>

    /**
     * Flow emitting the active academic cycle/semester (e.g., "2026-I").
     */
    val activeAcademicCycleFlow: Flow<String>

    /**
     * Sets the active academic cycle/semester.
     */
    suspend fun setActiveAcademicCycle(cycle: String): Result<Unit>

    /**
     * Flow emitting the first day of week ("MONDAY" or "SUNDAY").
     */
    val firstDayOfWeekFlow: Flow<String>

    /**
     * Sets the first day of week ("MONDAY" or "SUNDAY").
     */
    suspend fun setFirstDayOfWeek(day: String): Result<Unit>

    /**
     * Flow emitting whether completed evaluations should be shown in lists by default.
     */
    val showCompletedEvaluationsFlow: Flow<Boolean>

    /**
     * Sets whether completed evaluations should be shown.
     */
    suspend fun setShowCompletedEvaluations(show: Boolean): Result<Unit>

    /**
     * Exports all current preference keys and values as a string map.
     */
    suspend fun exportAll(): Map<String, String>

    /**
     * Imports preference values from a string map. Unknown keys are ignored.
     */
    suspend fun importAll(values: Map<String, String>): Result<Unit>
}

