package com.notifast.app.presentation.feature.settings

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.UserPrefsRepository
import com.notifast.app.domain.usecase.ConvertGradeScaleUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPrefsRepository: UserPrefsRepository,
    private val evaluationRepository: EvaluationRepository,
    private val convertGradeScaleUseCase: ConvertGradeScaleUseCase,
    private val rescheduleAllAlertsUseCase: com.notifast.app.domain.usecase.RescheduleAllAlertsUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private companion object {
        const val TAG = "SettingsViewModel"
        const val ERROR_GRADE_SCALE = "No se pudo actualizar la escala de calificación"
    }

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        observePreferences()
        calculateCacheSize()
        loadConvertibleEvaluationCount()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val userId = authRepository.getCurrentUserId()
            if (userId == null) {
                _uiState.update { it.copy(isLoading = false) }
                return@launch
            }
            authRepository.getUserProfile(userId).onSuccess { user ->
                _uiState.update {
                    it.copy(
                        userProfile = user,
                        lastSyncTimestampText = "Sincronizado ahora",
                        isLoading = false
                    )
                }
            }.onFailure {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun observePreferences() {
        viewModelScope.launch {
            userPrefsRepository.isDarkModeFlow.collect { isDark ->
                _uiState.update { it.copy(isDarkMode = isDark) }
            }
        }
        viewModelScope.launch {
            userPrefsRepository.themeAccentFlow.collect { accent ->
                _uiState.update { it.copy(themeAccent = accent) }
            }
        }
        viewModelScope.launch {
            userPrefsRepository.hapticsEnabledFlow.collect { enabled ->
                _uiState.update { it.copy(hapticsEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            userPrefsRepository.classAlertsEnabledFlow.collect { enabled ->
                _uiState.update { it.copy(classAlertsEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            userPrefsRepository.classAlertMinutesFlow.collect { minutes ->
                _uiState.update { it.copy(classAlertMinutes = minutes) }
            }
        }
        viewModelScope.launch {
            userPrefsRepository.evalAlertsEnabledFlow.collect { enabled ->
                _uiState.update { it.copy(evalAlertsEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            userPrefsRepository.evalAlertDaysFlow.collect { days ->
                _uiState.update { it.copy(evalAlertDays = days) }
            }
        }
        viewModelScope.launch {
            userPrefsRepository.dailySummaryEnabledFlow.collect { enabled ->
                _uiState.update { it.copy(dailySummaryEnabled = enabled) }
            }
        }
        viewModelScope.launch {
            userPrefsRepository.defaultGradeScaleFlow.collect { scale ->
                _uiState.update { it.copy(defaultGradeScale = scale) }
            }
        }
        viewModelScope.launch {
            userPrefsRepository.minPassingGradeFlow.collect { grade ->
                _uiState.update { it.copy(minPassingGrade = grade) }
            }
        }
        viewModelScope.launch {
            userPrefsRepository.activeAcademicCycleFlow.collect { cycle ->
                _uiState.update { it.copy(activeAcademicCycle = cycle) }
            }
        }
        viewModelScope.launch {
            userPrefsRepository.firstDayOfWeekFlow.collect { day ->
                _uiState.update { it.copy(firstDayOfWeek = day) }
            }
        }
    }

    private fun loadConvertibleEvaluationCount() {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            runCatching {
                evaluationRepository.getEvaluations(userId).first().count { it.grade != null }
            }.onSuccess { count ->
                _uiState.update { it.copy(evaluationsWithGradeCount = count) }
            }.onFailure { e ->
                Log.e(TAG, "Failed to load evaluations for grade-scale preview", e)
            }
        }
    }

    private fun calculateCacheSize() {
        viewModelScope.launch(Dispatchers.IO) {
            val totalBytes = SettingsCacheSupport.folderSize(context.cacheDir) +
                SettingsCacheSupport.folderSize(context.codeCacheDir)
            val formatted = SettingsCacheSupport.formatBytesToMb(totalBytes)
            _uiState.update { it.copy(cacheSizeBytes = totalBytes, cacheSizeFormatted = formatted) }
        }
    }

    private fun clearCache() {
        viewModelScope.launch(Dispatchers.IO) {
            SettingsCacheSupport.deleteFolderContents(context.cacheDir)
            val remainingBytes = SettingsCacheSupport.folderSize(context.cacheDir)
            val formatted = SettingsCacheSupport.formatBytesToMb(remainingBytes)
            _uiState.update {
                it.copy(
                    cacheSizeBytes = remainingBytes,
                    cacheSizeFormatted = formatted,
                    snackbarMessage = "Caché liberada correctamente"
                )
            }
        }
    }

    private fun changeGradeScale(targetScaleId: String, newPassing: Float) {
        viewModelScope.launch {
            val userId = authRepository.getCurrentUserId() ?: return@launch
            val currentScale = GradeScale.fromId(_uiState.value.defaultGradeScale)
            val target = GradeScale.fromId(targetScaleId)
            val custom = _uiState.value.minPassingGrade.takeIf { it != 0f }?.toDouble()
            val evals = evaluationRepository.getEvaluations(userId).first()
            val result = convertGradeScaleUseCase(evals, target, currentScale, custom)
            evaluationRepository.updateEvaluations(userId, result.convertedEvaluations).onFailure { e ->
                Log.e(TAG, "Failed to rewrite evaluations on grade scale change", e)
                _uiState.update { s -> s.copy(error = e.message ?: ERROR_GRADE_SCALE) }
                return@launch
            }
            userPrefsRepository.setDefaultGradeScale(target.id).onFailure { e ->
                Log.e(TAG, "Failed to persist default grade scale", e)
            }
            userPrefsRepository.setMinPassingGrade(newPassing).onFailure { e ->
                Log.e(TAG, "Failed to persist min passing grade", e)
            }
            _uiState.update {
                it.copy(
                    defaultGradeScale = target.id,
                    minPassingGrade = newPassing,
                    lastConvertedCount = result.convertedCount,
                    lastScaleLabel = target.label,
                    error = null
                )
            }
        }
    }

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.LoadProfile -> loadProfile()
            is SettingsEvent.RefreshCacheSize -> calculateCacheSize()
            is SettingsEvent.SetDarkMode -> viewModelScope.launch { userPrefsRepository.setDarkMode(event.enabled) }
            is SettingsEvent.SetThemeAccent -> viewModelScope.launch { userPrefsRepository.setThemeAccent(event.accent) }
            is SettingsEvent.SetHapticsEnabled -> viewModelScope.launch { userPrefsRepository.setHapticsEnabled(event.enabled) }
            is SettingsEvent.SetClassAlertsEnabled -> viewModelScope.launch {
                userPrefsRepository.setClassAlertsEnabled(event.enabled)
                rescheduleAllAlertsUseCase()
            }
            is SettingsEvent.SetClassAlertMinutes -> viewModelScope.launch {
                userPrefsRepository.setClassAlertMinutes(event.minutes)
                rescheduleAllAlertsUseCase()
            }
            is SettingsEvent.SetEvalAlertsEnabled -> viewModelScope.launch {
                userPrefsRepository.setEvalAlertsEnabled(event.enabled)
                rescheduleAllAlertsUseCase()
            }
            is SettingsEvent.SetEvalAlertDays -> viewModelScope.launch {
                userPrefsRepository.setEvalAlertDays(event.days)
                rescheduleAllAlertsUseCase()
            }
            is SettingsEvent.SetDailySummaryEnabled -> viewModelScope.launch {
                userPrefsRepository.setDailySummaryEnabled(event.enabled)
                rescheduleAllAlertsUseCase()
            }
            is SettingsEvent.ChangeGradeScale -> changeGradeScale(event.targetScaleId, event.minPassingGrade)
            is SettingsEvent.SetActiveAcademicCycle -> viewModelScope.launch { userPrefsRepository.setActiveAcademicCycle(event.cycle) }
            is SettingsEvent.SetFirstDayOfWeek -> viewModelScope.launch { userPrefsRepository.setFirstDayOfWeek(event.day) }
            is SettingsEvent.ClearCache -> clearCache()
            is SettingsEvent.OpenDialog -> _uiState.update { it.copy(activeDialog = event.dialog) }
            is SettingsEvent.DismissDialog -> _uiState.update { it.copy(activeDialog = null) }
            is SettingsEvent.ClearSnackbar -> _uiState.update {
                it.copy(snackbarMessage = null, error = null, lastScaleLabel = null)
            }
            is SettingsEvent.Logout -> logout()
        }
    }

    private fun logout() {
        viewModelScope.launch {
            authRepository.logout()
            _uiState.update { it.copy(isLoggedOut = true) }
        }
    }
}
