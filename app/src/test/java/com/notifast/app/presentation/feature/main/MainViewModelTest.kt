package com.notifast.app.presentation.feature.main

import android.content.Intent
import com.notifast.app.MainActivity
import com.notifast.app.domain.model.cloud.LocalSyncSummary
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.GoogleAccountRepository
import com.notifast.app.domain.repository.UserPrefsRepository
import com.notifast.app.domain.usecase.cloud.ObserveLocalSyncSummaryUseCase
import com.notifast.app.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
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

@OptIn(ExperimentalCoroutinesApi::class)
class MainViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val userPrefsRepository: UserPrefsRepository = mockk(relaxed = true)
    private val googleAccountRepository: GoogleAccountRepository = mockk(relaxed = true)
    private val observeLocalSyncSummaryUseCase: ObserveLocalSyncSummaryUseCase = mockk(relaxed = true)

    private val authStateFlow = MutableStateFlow<Boolean?>(null)
    private val isDarkModeFlow = MutableStateFlow<Boolean?>(null)
    private val hapticsEnabledFlow = MutableStateFlow(true)
    private val themePresetIdFlow = MutableStateFlow("classic")
    private val backgroundPresetIdFlow = MutableStateFlow("classic")
    private val customPrimaryHexFlow = MutableStateFlow<String?>(null)
    private val customBackgroundHexFlow = MutableStateFlow<String?>(null)

    @Before
    fun setUp() {
        every { authRepository.authState } returns authStateFlow
        every { userPrefsRepository.isDarkModeFlow } returns isDarkModeFlow
        every { userPrefsRepository.hapticsEnabledFlow } returns hapticsEnabledFlow
        every { userPrefsRepository.themePresetIdFlow } returns themePresetIdFlow
        every { userPrefsRepository.backgroundPresetIdFlow } returns backgroundPresetIdFlow
        every { userPrefsRepository.customPrimaryHexFlow } returns customPrimaryHexFlow
        every { userPrefsRepository.customBackgroundHexFlow } returns customBackgroundHexFlow
        every { googleAccountRepository.account } returns flowOf(null)
        coEvery { observeLocalSyncSummaryUseCase(any()) } returns flowOf(LocalSyncSummary.NO_ACCOUNT)
    }

    private fun createViewModel() = MainViewModel(
        authRepository = authRepository,
        userPrefsRepository = userPrefsRepository,
        googleAccountRepository = googleAccountRepository,
        observeLocalSyncSummaryUseCase = observeLocalSyncSummaryUseCase
    )

    @Test
    fun `initial isAppReady is false while authState is unresolved null`() = runTest {
        authStateFlow.value = null
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.isAppReady.value)
        assertFalse(viewModel.splashState.value.isAppReady)
    }

    @Test
    fun `isAppReady becomes true when authState emits authenticated true`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.isAppReady.value)

        authStateFlow.value = true
        advanceUntilIdle()

        assertTrue(viewModel.isAppReady.value)
        assertTrue(viewModel.splashState.value.isAppReady)
        assertEquals(true, viewModel.splashState.value.authState)
    }

    @Test
    fun `isAppReady becomes true when authState emits unauthenticated false`() = runTest {
        val viewModel = createViewModel()
        advanceUntilIdle()

        assertFalse(viewModel.isAppReady.value)

        authStateFlow.value = false
        advanceUntilIdle()

        assertTrue(viewModel.isAppReady.value)
        assertTrue(viewModel.splashState.value.isAppReady)
        assertEquals(false, viewModel.splashState.value.authState)
    }

    @Test
    fun `splashState captures active theme presets and custom colors accurately`() = runTest {
        themePresetIdFlow.value = "forest_sage"
        backgroundPresetIdFlow.value = "warm_mocha"
        isDarkModeFlow.value = true
        customPrimaryHexFlow.value = "#2E7D32"
        customBackgroundHexFlow.value = "#1B1B1B"
        authStateFlow.value = true

        val viewModel = createViewModel()
        advanceUntilIdle()

        val state = viewModel.splashState.value
        assertTrue(state.isAppReady)
        assertEquals("forest_sage", state.themePresetId)
        assertEquals("warm_mocha", state.backgroundPresetId)
        assertEquals(true, state.isDarkMode)
        assertEquals("#2E7D32", state.customPrimaryHex)
        assertEquals("#1B1B1B", state.customBackgroundHex)
        assertEquals("forest_sage", viewModel.themePresetId.value)
        assertEquals("warm_mocha", viewModel.backgroundPresetId.value)
    }

    @Test
    fun `handleNotificationIntent updates pendingDeepLink for schedules`() = runTest {
        val viewModel = createViewModel()
        val intent = mockk<Intent>(relaxed = true) {
            every { getStringExtra(MainActivity.EXTRA_DESTINATION) } returns MainActivity.DESTINATION_SCHEDULES
        }

        viewModel.handleNotificationIntent(intent)

        assertEquals(NotificationDeepLink.Schedules, viewModel.pendingDeepLink.value)

        viewModel.clearPendingDeepLink()
        assertNull(viewModel.pendingDeepLink.value)
    }

    @Test
    fun `handleNotificationIntent updates pendingDeepLink for subject detail with evaluation`() = runTest {
        val viewModel = createViewModel()
        val intent = mockk<Intent>(relaxed = true) {
            every { getStringExtra(MainActivity.EXTRA_DESTINATION) } returns MainActivity.DESTINATION_SUBJECT_DETAIL
            every { getStringExtra(MainActivity.EXTRA_SUBJECT_NAME) } returns "Cálculo I"
            every { getStringExtra(MainActivity.EXTRA_EVALUATION_ID) } returns "eval_123"
        }

        viewModel.handleNotificationIntent(intent)

        val link = viewModel.pendingDeepLink.value as NotificationDeepLink.SubjectDetail
        assertEquals("Cálculo I", link.subjectName)
        assertEquals("eval_123", link.evaluationId)
    }
}
