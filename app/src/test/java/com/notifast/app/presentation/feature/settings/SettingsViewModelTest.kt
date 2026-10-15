package com.notifast.app.presentation.feature.settings

import android.content.Context
import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.User
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.UserPrefsRepository
import com.notifast.app.domain.usecase.ConvertGradeScaleUseCase
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
    private val evaluationRepository: EvaluationRepository = mockk(relaxed = true)
    private val convertGradeScaleUseCase = ConvertGradeScaleUseCase()
    private val rescheduleAllAlertsUseCase: com.notifast.app.domain.usecase.RescheduleAllAlertsUseCase = mockk(relaxed = true)
    private val googleAccountRepository: com.notifast.app.domain.repository.GoogleAccountRepository = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)
    private val cacheDir: File = mockk(relaxed = true)

    private val sampleUser = TestDataFactory.createUser(
        uid = "test_user_id",
        firstName = "José",
        lastName = "Fernández",
        email = "jose@notifast.app"
    )

    private fun createSettingsViewModel(): SettingsViewModel =
        SettingsViewModel(
            authRepository = authRepository,
            userPrefsRepository = userPrefsRepository,
            evaluationRepository = evaluationRepository,
            convertGradeScaleUseCase = convertGradeScaleUseCase,
            rescheduleAllAlertsUseCase = rescheduleAllAlertsUseCase,
            googleAccountRepository = googleAccountRepository,
            context = context
        )

    @Before
    fun setUp() {
        coEvery { authRepository.getCurrentUserId() } returns "test_user_id"
        coEvery { authRepository.getUserProfile(any()) } returns Result.success(sampleUser)

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
        coEvery { googleAccountRepository.account } returns flowOf(null)
        coEvery { googleAccountRepository.unlink() } returns Result.success(Unit)
        coEvery { googleAccountRepository.signIn(any()) } returns Result.success(com.notifast.app.domain.repository.AuthStep.Linked(com.notifast.app.domain.model.cloud.CloudAccount("acc_1", "test@test.com", "Test", null)))
        coEvery { googleAccountRepository.completeAuthorization(any()) } returns Result.success(com.notifast.app.domain.model.cloud.CloudAccount("acc_1", "test@test.com", "Test", null))

        coEvery { evaluationRepository.getEvaluations(any()) } returns flowOf(emptyList())
        coEvery { evaluationRepository.updateEvaluations(any(), any()) } returns Result.success(Unit)
        coEvery { userPrefsRepository.setDefaultGradeScale(any()) } returns Result.success(Unit)
        coEvery { userPrefsRepository.setMinPassingGrade(any()) } returns Result.success(Unit)

        every { context.cacheDir } returns cacheDir
        every { cacheDir.listFiles() } returns emptyArray()
    }

    @Test
    fun `when initialized, loads user profile and observes all preferences correctly`() = runTest {
        val viewModel = createSettingsViewModel()
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

        val viewModel = createSettingsViewModel()
        advanceUntilIdle()

        viewModel.onEvent(SettingsEvent.SetDarkMode(true))
        advanceUntilIdle()

        coVerify { userPrefsRepository.setDarkMode(true) }
    }

    @Test
    fun `ChangeGradeScale converts evaluations and persists converted custom min passing`() = runTest {
        val evals = listOf(
            Evaluation(id = "1", grade = 15.0, isPresented = true, weight = 100.0, gradeScaleId = "0-20")
        )
        coEvery { authRepository.getCurrentUserId() } returns "user_123"
        coEvery { evaluationRepository.getEvaluations("user_123") } returns flowOf(evals)
        coEvery { evaluationRepository.updateEvaluations(any(), any()) } returns Result.success(Unit)
        coEvery { userPrefsRepository.setDefaultGradeScale(any()) } returns Result.success(Unit)
        coEvery { userPrefsRepository.setMinPassingGrade(any()) } returns Result.success(Unit)
        coEvery { userPrefsRepository.minPassingGradeFlow } returns flowOf(6.0f)
        coEvery { userPrefsRepository.defaultGradeScaleFlow } returns flowOf("0-20")

        val viewModel = createSettingsViewModel()
        advanceUntilIdle()

        viewModel.onEvent(SettingsEvent.ChangeGradeScale("0-100", 60f))
        advanceUntilIdle()

        coVerify { evaluationRepository.updateEvaluations("user_123", match { it.single().grade == 75.0 }) }
        coVerify { userPrefsRepository.setDefaultGradeScale("0-100") }
        // D16: custom 6.0 (0-20) converts to 30.0 (0-100) — dialog value is not written raw
        coVerify { userPrefsRepository.setMinPassingGrade(30f) }
        assertEquals(30f, viewModel.uiState.value.minPassingGrade, 0.01f)
    }

    @Test
    fun `ChangeGradeScale without custom min passing persists 0f sentinel`() = runTest {
        coEvery { authRepository.getCurrentUserId() } returns "user_123"
        coEvery { evaluationRepository.getEvaluations("user_123") } returns flowOf(emptyList())
        coEvery { evaluationRepository.updateEvaluations(any(), any()) } returns Result.success(Unit)
        coEvery { userPrefsRepository.setDefaultGradeScale(any()) } returns Result.success(Unit)
        coEvery { userPrefsRepository.setMinPassingGrade(any()) } returns Result.success(Unit)
        coEvery { userPrefsRepository.minPassingGradeFlow } returns flowOf(0f)
        coEvery { userPrefsRepository.defaultGradeScaleFlow } returns flowOf("0-10")

        val viewModel = createSettingsViewModel()
        advanceUntilIdle()

        viewModel.onEvent(SettingsEvent.ChangeGradeScale("0-20", 11f))
        advanceUntilIdle()

        coVerify { userPrefsRepository.setDefaultGradeScale("0-20") }
        coVerify { userPrefsRepository.setMinPassingGrade(0f) }
        coVerify(exactly = 0) { userPrefsRepository.setMinPassingGrade(11f) }
        assertEquals(0f, viewModel.uiState.value.minPassingGrade, 0.01f)
    }

    @Test
    fun `when setDefaultGradeScale fails, sets error and does not show success state`() = runTest {
        coEvery { authRepository.getCurrentUserId() } returns "user_123"
        coEvery { evaluationRepository.getEvaluations("user_123") } returns flowOf(emptyList())
        coEvery { evaluationRepository.updateEvaluations(any(), any()) } returns Result.success(Unit)
        coEvery { userPrefsRepository.setDefaultGradeScale(any()) } returns Result.failure(RuntimeException("prefs down"))
        coEvery { userPrefsRepository.minPassingGradeFlow } returns flowOf(0f)
        coEvery { userPrefsRepository.defaultGradeScaleFlow } returns flowOf("0-10")

        val viewModel = createSettingsViewModel()
        advanceUntilIdle()

        viewModel.onEvent(SettingsEvent.ChangeGradeScale("0-20", 11f))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("prefs down", state.error)
        assertNull(state.lastScaleLabel)
    }

    @Test
    fun `when setMinPassingGrade fails, sets error and does not show success state`() = runTest {
        coEvery { authRepository.getCurrentUserId() } returns "user_123"
        coEvery { evaluationRepository.getEvaluations("user_123") } returns flowOf(emptyList())
        coEvery { evaluationRepository.updateEvaluations(any(), any()) } returns Result.success(Unit)
        coEvery { userPrefsRepository.setDefaultGradeScale(any()) } returns Result.success(Unit)
        coEvery { userPrefsRepository.setMinPassingGrade(any()) } returns Result.failure(RuntimeException("min pass write failed"))
        coEvery { userPrefsRepository.minPassingGradeFlow } returns flowOf(0f)
        coEvery { userPrefsRepository.defaultGradeScaleFlow } returns flowOf("0-10")

        val viewModel = createSettingsViewModel()
        advanceUntilIdle()

        viewModel.onEvent(SettingsEvent.ChangeGradeScale("0-20", 11f))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals("min pass write failed", state.error)
        assertNull(state.lastScaleLabel)
    }

    @Test
    fun `when OpenDialog and DismissDialog events received, updates activeDialog state`() = runTest {
        val viewModel = createSettingsViewModel()
        advanceUntilIdle()

        viewModel.onEvent(SettingsEvent.OpenDialog(SettingsDialogType.THEME))
        assertEquals(SettingsDialogType.THEME, viewModel.uiState.value.activeDialog)

        viewModel.onEvent(SettingsEvent.DismissDialog)
        assertNull(viewModel.uiState.value.activeDialog)
    }

    @Test
    fun `when Logout event received, calls authRepository logout and sets isLoggedOut`() = runTest {
        val viewModel = createSettingsViewModel()
        advanceUntilIdle()

        viewModel.onEvent(SettingsEvent.Logout)
        advanceUntilIdle()

        coVerify { authRepository.logout() }
        assertTrue(viewModel.uiState.value.isLoggedOut)
    }

    @Test
    fun `when initialized with google account, observes account into uiState`() = runTest {
        val cloudAccount = com.notifast.app.domain.model.cloud.CloudAccount("acc_123", "user@notifast.app", "Test User", null)
        coEvery { googleAccountRepository.account } returns flowOf(cloudAccount)

        val viewModel = createSettingsViewModel()
        advanceUntilIdle()

        assertEquals(cloudAccount, viewModel.uiState.value.cloudAccount)
    }

    @Test
    fun `when UnlinkAccount received, calls googleAccountRepository unlink and leaves local repos untouched`() = runTest {
        val viewModel = createSettingsViewModel()
        advanceUntilIdle()

        viewModel.onEvent(SettingsEvent.UnlinkAccount)
        advanceUntilIdle()

        coVerify(exactly = 1) { googleAccountRepository.unlink() }
        coVerify(exactly = 0) { authRepository.logout() }
        coVerify(exactly = 0) { evaluationRepository.updateEvaluations(any(), any()) }
    }

    @Test
    fun `when SwitchAccount received, unlinks and then starts signIn`() = runTest {
        val viewModel = createSettingsViewModel()
        advanceUntilIdle()

        viewModel.onEvent(SettingsEvent.SwitchAccount(context))
        advanceUntilIdle()

        io.mockk.coVerifyOrder {
            googleAccountRepository.unlink()
            googleAccountRepository.signIn(context)
        }
    }

    @Test
    fun `when LinkAccount received, starts signIn and sets pendingAuthIntent if needs resolution`() = runTest {
        val mockPendingIntent: android.app.PendingIntent = mockk()
        coEvery { googleAccountRepository.signIn(context) } returns Result.success(
            com.notifast.app.domain.repository.AuthStep.NeedsResolution(mockPendingIntent)
        )

        val viewModel = createSettingsViewModel()
        advanceUntilIdle()

        viewModel.onEvent(SettingsEvent.LinkAccount(context))
        advanceUntilIdle()

        assertEquals(mockPendingIntent, viewModel.uiState.value.pendingAuthIntent)
    }

    @Test
    fun `when AuthResult received, calls completeAuthorization`() = runTest {
        val mockIntent: android.content.Intent = mockk()
        val viewModel = createSettingsViewModel()
        advanceUntilIdle()

        viewModel.onEvent(SettingsEvent.AuthResult(mockIntent))
        advanceUntilIdle()

        coVerify(exactly = 1) { googleAccountRepository.completeAuthorization(mockIntent) }
        assertNull(viewModel.uiState.value.pendingAuthIntent)
    }
}
