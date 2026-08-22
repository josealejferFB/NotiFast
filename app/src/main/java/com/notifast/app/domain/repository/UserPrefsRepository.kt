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
     * Flow emitting the default grading scale format (e.g., "0-10", "0-20", "0-100", "A-F").
     */
    val defaultGradeScaleFlow: Flow<String>

    /**
     * Sets the default grading scale format.
     */
    suspend fun setDefaultGradeScale(scale: String): Result<Unit>

    /**
     * Flow emitting whether completed evaluations should be shown in lists by default.
     */
    val showCompletedEvaluationsFlow: Flow<Boolean>

    /**
     * Sets whether completed evaluations should be shown.
     */
    suspend fun setShowCompletedEvaluations(show: Boolean): Result<Unit>
}
