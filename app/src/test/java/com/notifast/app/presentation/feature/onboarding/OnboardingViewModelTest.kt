package com.notifast.app.presentation.feature.onboarding

import android.net.Uri
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository: AuthRepository = mockk(relaxed = true)
    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setUp() {
        viewModel = OnboardingViewModel(authRepository)
    }

    @Test
    fun `initial state has default values`() {
        val state = viewModel.uiState.value
        assertEquals("", state.firstName)
        assertEquals("", state.lastName)
        assertNull(state.profileImageUri)
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertFalse(state.isCompleted)
    }

    @Test
    fun `FirstNameChanged event updates firstName`() {
        viewModel.onEvent(OnboardingEvent.FirstNameChanged("Carlos"))

        val state = viewModel.uiState.value
        assertEquals("Carlos", state.firstName)
    }

    @Test
    fun `LastNameChanged event updates lastName`() {
        viewModel.onEvent(OnboardingEvent.LastNameChanged("González"))

        val state = viewModel.uiState.value
        assertEquals("González", state.lastName)
    }

    @Test
    fun `ImageSelected event updates profileImageUri`() {
        val mockUri = mockk<Uri>()
        viewModel.onEvent(OnboardingEvent.ImageSelected(mockUri))

        val state = viewModel.uiState.value
        assertEquals(mockUri, state.profileImageUri)
    }

    @Test
    fun `ClearError event clears error message`() {
        viewModel.onEvent(OnboardingEvent.FirstNameChanged("   "))
        viewModel.onEvent(OnboardingEvent.SubmitProfile)
        assertEquals("Por favor escribe tu nombre para continuar", viewModel.uiState.value.error)

        viewModel.onEvent(OnboardingEvent.ClearError)
        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `SubmitProfile with blank firstName sets error and does not call repository`() = runTest {
        viewModel.onEvent(OnboardingEvent.FirstNameChanged("   "))
        viewModel.onEvent(OnboardingEvent.SubmitProfile)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isCompleted)
        assertEquals("Por favor escribe tu nombre para continuar", state.error)

        coVerify(exactly = 0) { authRepository.createLocalProfile(any(), any(), any()) }
    }

    @Test
    fun `SubmitProfile success trims inputs, calls repository and marks isCompleted true`() = runTest {
        val mockUri = mockk<Uri>()
        coEvery {
            authRepository.createLocalProfile("Carlos", "Gómez", mockUri)
        } returns Result.success("local-uid-123")

        viewModel.onEvent(OnboardingEvent.FirstNameChanged("  Carlos  "))
        viewModel.onEvent(OnboardingEvent.LastNameChanged("  Gómez  "))
        viewModel.onEvent(OnboardingEvent.ImageSelected(mockUri))
        viewModel.onEvent(OnboardingEvent.SubmitProfile)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isCompleted)
        assertNull(state.error)

        coVerify(exactly = 1) {
            authRepository.createLocalProfile("Carlos", "Gómez", mockUri)
        }
    }

    @Test
    fun `SubmitProfile repository failure sets error and stops loading`() = runTest {
        coEvery {
            authRepository.createLocalProfile(any(), any(), any())
        } returns Result.failure(Exception("Error al crear perfil local"))

        viewModel.onEvent(OnboardingEvent.FirstNameChanged("María"))
        viewModel.onEvent(OnboardingEvent.SubmitProfile)

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isCompleted)
        assertEquals("Error al crear perfil local", state.error)
    }
}
