package com.notifast.app.presentation.feature.settings

import android.content.Context
import com.notifast.app.data.model.User
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.UserPrefsRepository
import com.notifast.app.util.MainDispatcherRule
import com.notifast.app.util.TestDataFactory
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val userPrefsRepository: UserPrefsRepository = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)
    private val cacheDir: File = mockk(relaxed = true)

    private val sampleUser = TestDataFactory.createUser(
        uid = "test_user_id",
        firstName = "José",
        lastName = "Fernández",
        email = "jose@notifast.app"
    )

    @Before
    fun setUp() {
        coEvery { authRepository.getCurrentUserId() } returns "test_user_id"
        coEvery { authRepository.getUserProfile("test_user_id") } returns Result.success(sampleUser)

        coEvery { userPrefsRepository.isDarkModeFlow } returns flowOf(null)
        coEvery { userPrefsRepository.themeAccentFlow } returns flowOf("aqua")
        coEvery { userPrefsRepository.hapticsEnabledFlow } returns flowOf(true)
        coEvery { userPrefsRepository.classAlertsEnabledFlow } returns flowOf(true)
        coEvery { userPrefsRepository.classAlertMinutesFlow } returns flowOf(15)
        coEvery { userPrefsRepository.evalAlertsEnabledFlow } returns flowOf(true)
        coEvery { userPrefsRepository.evalAlertDaysFlow } returns flowOf(1)
        coEvery { userPrefsRepository.dailySummaryEnabledFlow } returns flowOf(false)
        coEvery { userPrefsRepository.defaultGradeScaleFlow } returns flowOf("0-20")
        coEvery { userPrefsRepository.minPassingGradeFlow } returns flowOf(11.0f)
        coEvery { userPrefsRepository.activeAcademicCycleFlow } returns flowOf("2026-I")
        coEvery { userPrefsRepository.firstDayOfWeekFlow } returns flowOf("MONDAY")

        every { context.cacheDir } returns cacheDir
        every { cacheDir.listFiles() } returns emptyArray()
    }

    @Test
    fun `when initialized, loads user profile and observes all preferences correctly`() = runTest {
        val viewModel = SettingsViewModel(authRepository, userPrefsRepository, context)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(sampleUser, state.userProfile)
        assertNull(state.isDarkMode)
        assertEquals("aqua", state.themeAccent)
        assertTrue(state.hapticsEnabled)
        assertTrue(state.classAlertsEnabled)
        assertEquals(15, state.classAlertMinutes)
        assertTrue(state.evalAlertsEnabled)
        assertEquals(1, state.evalAlertDays)
        assertFalse(state.dailySummaryEnabled)
        assertEquals("0-20", state.defaultGradeScale)
        assertEquals(11.0f, state.minPassingGrade, 0.01f)
        assertEquals("2026-I", state.activeAcademicCycle)
        assertEquals("MONDAY", state.firstDayOfWeek)
        assertFalse(state.isLoading)
    }

    @Test
    fun `when SetDarkMode event received, delegates to UserPrefsRepository`() = runTest {
        coEvery { userPrefsRepository.setDarkMode(true) } returns Result.success(Unit)

        val viewModel = SettingsViewModel(authRepository, userPrefsRepository, context)
        advanceUntilIdle()

        viewModel.onEvent(SettingsEvent.SetDarkMode(true))
        advanceUntilIdle()

        coVerify { userPrefsRepository.setDarkMode(true) }
    }

    @Test
    fun `when SetDefaultGradeScale event received, delegates to UserPrefsRepository`() = runTest {
        coEvery { userPrefsRepository.setDefaultGradeScale("0-10") } returns Result.success(Unit)

        val viewModel = SettingsViewModel(authRepository, userPrefsRepository, context)
        advanceUntilIdle()

        viewModel.onEvent(SettingsEvent.SetDefaultGradeScale("0-10"))
        advanceUntilIdle()

        coVerify { userPrefsRepository.setDefaultGradeScale("0-10") }
    }

    @Test
    fun `when SetMinPassingGrade event received, delegates to UserPrefsRepository`() = runTest {
        coEvery { userPrefsRepository.setMinPassingGrade(13.0f) } returns Result.success(Unit)

        val viewModel = SettingsViewModel(authRepository, userPrefsRepository, context)
        advanceUntilIdle()

        viewModel.onEvent(SettingsEvent.SetMinPassingGrade(13.0f))
        advanceUntilIdle()

        coVerify { userPrefsRepository.setMinPassingGrade(13.0f) }
    }

    @Test
    fun `when OpenDialog and DismissDialog events received, updates activeDialog state`() = runTest {
        val viewModel = SettingsViewModel(authRepository, userPrefsRepository, context)
        advanceUntilIdle()

        viewModel.onEvent(SettingsEvent.OpenDialog(SettingsDialogType.THEME))
        assertEquals(SettingsDialogType.THEME, viewModel.uiState.value.activeDialog)

        viewModel.onEvent(SettingsEvent.DismissDialog)
        assertNull(viewModel.uiState.value.activeDialog)
    }

    @Test
    fun `when Logout event received, calls authRepository logout and sets isLoggedOut`() = runTest {
        val viewModel = SettingsViewModel(authRepository, userPrefsRepository, context)
        advanceUntilIdle()

        viewModel.onEvent(SettingsEvent.Logout)
        advanceUntilIdle()

        coVerify { authRepository.logout() }
        assertTrue(viewModel.uiState.value.isLoggedOut)
    }
}
