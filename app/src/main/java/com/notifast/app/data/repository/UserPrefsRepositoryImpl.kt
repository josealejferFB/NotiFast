package com.notifast.app.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.notifast.app.domain.repository.UserPrefsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
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
        val DEFAULT_GRADE_SCALE = stringPreferencesKey("default_grade_scale")
        val SHOW_COMPLETED_EVALUATIONS = booleanPreferencesKey("show_completed_evaluations")
    }

    override val isDarkModeFlow: Flow<Boolean?>
        get() = dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    Log.e(TAG, "Error reading dark mode preference", exception)
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
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

    override val defaultGradeScaleFlow: Flow<String>
        get() = dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    Log.e(TAG, "Error reading default grade scale preference", exception)
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
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

    override val showCompletedEvaluationsFlow: Flow<Boolean>
        get() = dataStore.data
            .catch { exception ->
                if (exception is IOException) {
                    Log.e(TAG, "Error reading show completed evaluations preference", exception)
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }
            .map { preferences ->
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
}
