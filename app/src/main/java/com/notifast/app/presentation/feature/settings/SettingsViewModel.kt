package com.notifast.app.presentation.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.UserPrefsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userPrefsRepository: UserPrefsRepository,
    private val rescheduleAllAlertsUseCase: com.notifast.app.domain.usecase.RescheduleAllAlertsUseCase,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
        observePreferences()
        calculateCacheSize()
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

    private fun calculateCacheSize() {
        viewModelScope.launch(Dispatchers.IO) {
            val totalBytes = getFolderSize(context.cacheDir) + getFolderSize(context.codeCacheDir)
            val formatted = formatBytesToMb(totalBytes)
            _uiState.update { it.copy(cacheSizeBytes = totalBytes, cacheSizeFormatted = formatted) }
        }
    }

    private fun getFolderSize(dir: File?): Long {
        if (dir == null || !dir.exists()) return 0L
        var size = 0L
        dir.listFiles()?.forEach { file ->
            size += if (file.isDirectory) getFolderSize(file) else file.length()
        }
        return size
    }

    private fun formatBytesToMb(bytes: Long): String {
        val mb = bytes.toDouble() / (1024.0 * 1024.0)
        return String.format(Locale.US, "%.1f MB", mb)
    }

    private fun clearCache() {
        viewModelScope.launch(Dispatchers.IO) {
            deleteFolderContents(context.cacheDir)
            val remainingBytes = getFolderSize(context.cacheDir)
            val formatted = formatBytesToMb(remainingBytes)
            _uiState.update {
                it.copy(
                    cacheSizeBytes = remainingBytes,
                    cacheSizeFormatted = formatted,
                    snackbarMessage = "Caché liberada correctamente"
                )
            }
        }
    }

    private fun deleteFolderContents(dir: File?) {
        if (dir == null || !dir.exists()) return
        dir.listFiles()?.forEach { file ->
            if (file.isDirectory) deleteFolderContents(file)
            file.delete()
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
            is SettingsEvent.SetDefaultGradeScale -> viewModelScope.launch { userPrefsRepository.setDefaultGradeScale(event.scale) }
            is SettingsEvent.SetMinPassingGrade -> viewModelScope.launch { userPrefsRepository.setMinPassingGrade(event.grade) }
            is SettingsEvent.SetActiveAcademicCycle -> viewModelScope.launch { userPrefsRepository.setActiveAcademicCycle(event.cycle) }
            is SettingsEvent.SetFirstDayOfWeek -> viewModelScope.launch { userPrefsRepository.setFirstDayOfWeek(event.day) }
            is SettingsEvent.ClearCache -> clearCache()
            is SettingsEvent.ClearCache -> clearCache()
            is SettingsEvent.OpenDialog -> _uiState.update { it.copy(activeDialog = event.dialog) }
            is SettingsEvent.DismissDialog -> _uiState.update { it.copy(activeDialog = null) }
            is SettingsEvent.ClearSnackbar -> _uiState.update { it.copy(snackbarMessage = null) }
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
