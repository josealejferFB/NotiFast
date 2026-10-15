package com.notifast.app.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.notifast.app.domain.repository.UserPrefsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

val Context.userPrefsDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

private const val TAG = "UserPrefsRepository"

@Singleton
class UserPrefsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : UserPrefsRepository {

    private object PreferencesKeys {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val HAS_DARK_MODE_SET = booleanPreferencesKey("has_dark_mode_set")
        val THEME_ACCENT = stringPreferencesKey("theme_accent")
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val CLASS_ALERTS_ENABLED = booleanPreferencesKey("class_alerts_enabled")
        val CLASS_ALERT_MINUTES = intPreferencesKey("class_alert_minutes")
        val EVAL_ALERTS_ENABLED = booleanPreferencesKey("eval_alerts_enabled")
        val EVAL_ALERT_DAYS = intPreferencesKey("eval_alert_days")
        val DAILY_SUMMARY_ENABLED = booleanPreferencesKey("daily_summary_enabled")
        val DEFAULT_GRADE_SCALE = stringPreferencesKey("default_grade_scale")
        val MIN_PASSING_GRADE = floatPreferencesKey("min_passing_grade")
        val ACTIVE_ACADEMIC_CYCLE = stringPreferencesKey("active_academic_cycle")
        val FIRST_DAY_OF_WEEK = stringPreferencesKey("first_day_of_week")
        val SHOW_COMPLETED_EVALUATIONS = booleanPreferencesKey("show_completed_evaluations")
    }

    private fun <T> safeDataFlow(mapper: (Preferences) -> T): Flow<T> {
        return dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    Log.e(TAG, "Error reading datastore preferences", exception)
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map(mapper)
    }

    override val isDarkModeFlow: Flow<Boolean?>
        get() = safeDataFlow { preferences ->
            if (preferences[PreferencesKeys.HAS_DARK_MODE_SET] == true) {
                preferences[PreferencesKeys.IS_DARK_MODE]
            } else {
                null
            }
        }

    override suspend fun setDarkMode(enabled: Boolean?): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                if (enabled == null) {
                    preferences[PreferencesKeys.HAS_DARK_MODE_SET] = false
                    preferences.remove(PreferencesKeys.IS_DARK_MODE)
                } else {
                    preferences[PreferencesKeys.HAS_DARK_MODE_SET] = true
                    preferences[PreferencesKeys.IS_DARK_MODE] = enabled
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save dark mode preference", e)
            Result.failure(e)
        }
    }

    override val themeAccentFlow: Flow<String>
        get() = safeDataFlow { preferences ->
            preferences[PreferencesKeys.THEME_ACCENT] ?: "aqua"
        }

    override suspend fun setThemeAccent(accent: String): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.THEME_ACCENT] = accent
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save theme accent preference", e)
            Result.failure(e)
        }
    }

    override val hapticsEnabledFlow: Flow<Boolean>
        get() = safeDataFlow { preferences ->
            preferences[PreferencesKeys.HAPTICS_ENABLED] ?: true
        }

    override suspend fun setHapticsEnabled(enabled: Boolean): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.HAPTICS_ENABLED] = enabled
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save haptics preference", e)
            Result.failure(e)
        }
    }

    override val classAlertsEnabledFlow: Flow<Boolean>
        get() = safeDataFlow { preferences ->
            preferences[PreferencesKeys.CLASS_ALERTS_ENABLED] ?: true
        }

    override suspend fun setClassAlertsEnabled(enabled: Boolean): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.CLASS_ALERTS_ENABLED] = enabled
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save class alerts preference", e)
            Result.failure(e)
        }
    }

    override val classAlertMinutesFlow: Flow<Int>
        get() = safeDataFlow { preferences ->
            preferences[PreferencesKeys.CLASS_ALERT_MINUTES] ?: 15
        }

    override suspend fun setClassAlertMinutes(minutes: Int): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.CLASS_ALERT_MINUTES] = minutes
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save class alert minutes preference", e)
            Result.failure(e)
        }
    }

    override val evalAlertsEnabledFlow: Flow<Boolean>
        get() = safeDataFlow { preferences ->
            preferences[PreferencesKeys.EVAL_ALERTS_ENABLED] ?: true
        }

    override suspend fun setEvalAlertsEnabled(enabled: Boolean): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.EVAL_ALERTS_ENABLED] = enabled
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save eval alerts preference", e)
            Result.failure(e)
        }
    }

    override val evalAlertDaysFlow: Flow<Int>
        get() = safeDataFlow { preferences ->
            preferences[PreferencesKeys.EVAL_ALERT_DAYS] ?: 1
        }

    override suspend fun setEvalAlertDays(days: Int): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.EVAL_ALERT_DAYS] = days
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save eval alert days preference", e)
            Result.failure(e)
        }
    }

    override val dailySummaryEnabledFlow: Flow<Boolean>
        get() = safeDataFlow { preferences ->
            preferences[PreferencesKeys.DAILY_SUMMARY_ENABLED] ?: false
        }

    override suspend fun setDailySummaryEnabled(enabled: Boolean): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.DAILY_SUMMARY_ENABLED] = enabled
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save daily summary preference", e)
            Result.failure(e)
        }
    }

    override val defaultGradeScaleFlow: Flow<String>
        get() = safeDataFlow { preferences ->
            preferences[PreferencesKeys.DEFAULT_GRADE_SCALE] ?: "0-10"
        }

    override suspend fun setDefaultGradeScale(scale: String): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.DEFAULT_GRADE_SCALE] = scale
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save default grade scale preference", e)
            Result.failure(e)
        }
    }

    override val minPassingGradeFlow: Flow<Float>
        get() = safeDataFlow { preferences ->
            // 0f = "use GradeScale default". A non-zero value is a user override.
            preferences[PreferencesKeys.MIN_PASSING_GRADE] ?: 0f
        }

    override suspend fun setMinPassingGrade(grade: Float): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.MIN_PASSING_GRADE] = grade
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save min passing grade preference", e)
            Result.failure(e)
        }
    }

    override val activeAcademicCycleFlow: Flow<String>
        get() = safeDataFlow { preferences ->
            preferences[PreferencesKeys.ACTIVE_ACADEMIC_CYCLE] ?: "2026-I"
        }

    override suspend fun setActiveAcademicCycle(cycle: String): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.ACTIVE_ACADEMIC_CYCLE] = cycle
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save active academic cycle preference", e)
            Result.failure(e)
        }
    }

    override val firstDayOfWeekFlow: Flow<String>
        get() = safeDataFlow { preferences ->
            preferences[PreferencesKeys.FIRST_DAY_OF_WEEK] ?: "MONDAY"
        }

    override suspend fun setFirstDayOfWeek(day: String): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.FIRST_DAY_OF_WEEK] = day
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save first day of week preference", e)
            Result.failure(e)
        }
    }

    override val showCompletedEvaluationsFlow: Flow<Boolean>
        get() = safeDataFlow { preferences ->
            preferences[PreferencesKeys.SHOW_COMPLETED_EVALUATIONS] ?: true
        }

    override suspend fun setShowCompletedEvaluations(show: Boolean): Result<Unit> {
        return try {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.SHOW_COMPLETED_EVALUATIONS] = show
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save show completed evaluations preference", e)
            Result.failure(e)
        }
    }

    override suspend fun exportAll(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        try {
            val prefs = safeDataFlow { it }.first()
            prefs[PreferencesKeys.IS_DARK_MODE]?.let { map["is_dark_mode"] = it.toString() }
            prefs[PreferencesKeys.HAS_DARK_MODE_SET]?.let { map["has_dark_mode_set"] = it.toString() }
            prefs[PreferencesKeys.THEME_ACCENT]?.let { map["theme_accent"] = it }
            prefs[PreferencesKeys.HAPTICS_ENABLED]?.let { map["haptics_enabled"] = it.toString() }
            prefs[PreferencesKeys.CLASS_ALERTS_ENABLED]?.let { map["class_alerts_enabled"] = it.toString() }
            prefs[PreferencesKeys.CLASS_ALERT_MINUTES]?.let { map["class_alert_minutes"] = it.toString() }
            prefs[PreferencesKeys.EVAL_ALERTS_ENABLED]?.let { map["eval_alerts_enabled"] = it.toString() }
            prefs[PreferencesKeys.EVAL_ALERT_DAYS]?.let { map["eval_alert_days"] = it.toString() }
            prefs[PreferencesKeys.DAILY_SUMMARY_ENABLED]?.let { map["daily_summary_enabled"] = it.toString() }
            prefs[PreferencesKeys.DEFAULT_GRADE_SCALE]?.let { map["default_grade_scale"] = it }
            prefs[PreferencesKeys.MIN_PASSING_GRADE]?.let { map["min_passing_grade"] = it.toString() }
            prefs[PreferencesKeys.ACTIVE_ACADEMIC_CYCLE]?.let { map["active_academic_cycle"] = it }
            prefs[PreferencesKeys.FIRST_DAY_OF_WEEK]?.let { map["first_day_of_week"] = it }
            prefs[PreferencesKeys.SHOW_COMPLETED_EVALUATIONS]?.let { map["show_completed_evaluations"] = it.toString() }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export preferences", e)
        }
        return map
    }

    override suspend fun importAll(values: Map<String, String>): Result<Unit> {
        return try {
            dataStore.edit { prefs ->
                values["is_dark_mode"]?.toBooleanStrictOrNull()?.let { prefs[PreferencesKeys.IS_DARK_MODE] = it }
                values["has_dark_mode_set"]?.toBooleanStrictOrNull()?.let { prefs[PreferencesKeys.HAS_DARK_MODE_SET] = it }
                values["theme_accent"]?.let { prefs[PreferencesKeys.THEME_ACCENT] = it }
                values["haptics_enabled"]?.toBooleanStrictOrNull()?.let { prefs[PreferencesKeys.HAPTICS_ENABLED] = it }
                values["class_alerts_enabled"]?.toBooleanStrictOrNull()?.let { prefs[PreferencesKeys.CLASS_ALERTS_ENABLED] = it }
                values["class_alert_minutes"]?.toIntOrNull()?.let { prefs[PreferencesKeys.CLASS_ALERT_MINUTES] = it }
                values["eval_alerts_enabled"]?.toBooleanStrictOrNull()?.let { prefs[PreferencesKeys.EVAL_ALERTS_ENABLED] = it }
                values["eval_alert_days"]?.toIntOrNull()?.let { prefs[PreferencesKeys.EVAL_ALERT_DAYS] = it }
                values["daily_summary_enabled"]?.toBooleanStrictOrNull()?.let { prefs[PreferencesKeys.DAILY_SUMMARY_ENABLED] = it }
                values["default_grade_scale"]?.let { prefs[PreferencesKeys.DEFAULT_GRADE_SCALE] = it }
                values["min_passing_grade"]?.toFloatOrNull()?.let { prefs[PreferencesKeys.MIN_PASSING_GRADE] = it }
                values["active_academic_cycle"]?.let { prefs[PreferencesKeys.ACTIVE_ACADEMIC_CYCLE] = it }
                values["first_day_of_week"]?.let { prefs[PreferencesKeys.FIRST_DAY_OF_WEEK] = it }
                values["show_completed_evaluations"]?.toBooleanStrictOrNull()?.let { prefs[PreferencesKeys.SHOW_COMPLETED_EVALUATIONS] = it }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import preferences", e)
            Result.failure(e)
        }
    }
}
