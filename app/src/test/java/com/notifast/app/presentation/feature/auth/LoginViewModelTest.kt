package com.notifast.app.presentation.feature.auth

import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.util.MainDispatcherRule
import io.mockk.coEvery
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
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val authRepository: AuthRepository = mockk(relaxed = true)
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        viewModel = LoginViewModel(authRepository)
    }

    @Test
    fun `initial state has default values`() {
        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isLoggedIn)
        assertNull(state.error)
    }

    @Test
    fun `login success updates state to isLoggedIn true`() = runTest {
        coEvery { authRepository.login("test@example.com", "password123") } returns Result.success("user-1")

        viewModel.onEvent(LoginEvent.LoginWithEmail("test@example.com", "password123"))

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isLoggedIn)
        assertNull(state.error)
    }

    @Test
    fun `login failure updates error in state`() = runTest {
        coEvery { authRepository.login("test@example.com", "wrong") } returns Result.failure(Exception("Credenciales inválidas"))

        viewModel.onEvent(LoginEvent.LoginWithEmail("test@example.com", "wrong"))

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertFalse(state.isLoggedIn)
        assertEquals("Credenciales inválidas", state.error)
    }

    @Test
    fun `signUp success updates state to isLoggedIn true`() = runTest {
        coEvery { authRepository.signUp("new@example.com", "pass", "Juan", "Perez", null) } returns Result.success("user-2")

        viewModel.onEvent(LoginEvent.SignUpWithDetails("new@example.com", "pass", "Juan", "Perez", null))

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isLoggedIn)
        assertNull(state.error)
    }

    @Test
    fun `google sign in success updates state to isLoggedIn true`() = runTest {
        coEvery { authRepository.signInWithGoogle("token-123") } returns Result.success("user-google")

        viewModel.onEvent(LoginEvent.GoogleSignIn("token-123"))

        val state = viewModel.uiState.value
        assertFalse(state.isLoading)
        assertTrue(state.isLoggedIn)
        assertNull(state.error)
    }

    @Test
    fun `google error updates state error message`() = runTest {
        viewModel.onEvent(LoginEvent.GoogleError("Cancelado por el usuario"))

        val state = viewModel.uiState.value
        assertEquals("Cancelado por el usuario", state.error)
    }
}
