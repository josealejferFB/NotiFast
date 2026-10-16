package com.notifast.app.presentation.feature.onboarding

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.notifast.app.domain.model.cloud.CloudAccount
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.AuthStep
import com.notifast.app.domain.repository.CloudAuthException
import com.notifast.app.domain.repository.GoogleAccountRepository
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
    private val googleAccountRepository: GoogleAccountRepository = mockk(relaxed = true)
    private lateinit var viewModel: OnboardingViewModel

    @Before
    fun setUp() {
        viewModel = OnboardingViewModel(authRepository, googleAccountRepository)
    }

    @Test
    fun `initial state has default values`() {
        val state = viewModel.uiState.value
        assertEquals("", state.firstName)
        assertEquals("", state.lastName)
        assertNull(state.profileImageUri)
        assertFalse(state.isLoading)
        assertFalse(state.isGoogleLoading)
        assertNull(state.pendingAuthIntent)
        assertNull(state.error)
        assertFalse(state.isCompleted)
        assertFalse(state.autoOpenCloudHub)
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
        assertFalse(state.autoOpenCloudHub)
        assertEquals("Por favor escribe tu nombre para continuar", state.error)

        coVerify(exactly = 0) { authRepository.createLocalProfile(any(), any(), any()) }
    }

    @Test
    fun `SubmitProfile success trims inputs, calls repository and marks isCompleted true with autoOpenCloudHub false`() = runTest {
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
        assertFalse(state.autoOpenCloudHub)
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
        assertFalse(state.autoOpenCloudHub)
        assertEquals("Error al crear perfil local.", state.error)
    }

    @Test
    fun `SignInWithGoogle with NeedsResolution sets pendingAuthIntent and clears isGoogleLoading`() = runTest {
        val mockContext = mockk<Context>(relaxed = true)
        val mockPendingIntent = mockk<PendingIntent>()
        coEvery {
            googleAccountRepository.signIn(mockContext)
        } returns Result.success(AuthStep.NeedsResolution(mockPendingIntent))

        viewModel.onEvent(OnboardingEvent.SignInWithGoogle(mockContext))

        val state = viewModel.uiState.value
        assertFalse(state.isGoogleLoading)
        assertEquals(mockPendingIntent, state.pendingAuthIntent)
        assertNull(state.error)
        assertFalse(state.isCompleted)
    }

    @Test
    fun `SignInWithGoogle with Linked directly creates local profile and completes with autoOpenCloudHub true`() = runTest {
        val mockContext = mockk<Context>(relaxed = true)
        val account = CloudAccount(
            accountId = "google-id-123",
            email = "laura.torres@gmail.com",
            displayName = "Laura Torres",
            photoUrl = null
        )
        coEvery {
            googleAccountRepository.signIn(mockContext)
        } returns Result.success(AuthStep.Linked(account))
        coEvery {
            authRepository.createLocalProfile("Laura", "Torres", any())
        } returns Result.success("user-123")

        viewModel.onEvent(OnboardingEvent.SignInWithGoogle(mockContext))

        val state = viewModel.uiState.value
        assertFalse(state.isGoogleLoading)
        assertTrue(state.isCompleted)
        assertTrue(state.autoOpenCloudHub)
        assertNull(state.error)

        coVerify(exactly = 1) {
            authRepository.createLocalProfile("Laura", "Torres", null)
        }
    }

    @Test
    fun `SignInWithGoogle failure sets error and stops loading`() = runTest {
        val mockContext = mockk<Context>(relaxed = true)
        coEvery {
            googleAccountRepository.signIn(mockContext)
        } returns Result.failure(Exception("Fallo en servicios de Google"))

        viewModel.onEvent(OnboardingEvent.SignInWithGoogle(mockContext))

        val state = viewModel.uiState.value
        assertFalse(state.isGoogleLoading)
        assertFalse(state.isCompleted)
        assertEquals("Fallo en servicios de Google.", state.error)
    }

    @Test
    fun `GoogleAuthResult with null cancels loading without setting error`() = runTest {
        viewModel.onEvent(OnboardingEvent.GoogleAuthResult(null))

        val state = viewModel.uiState.value
        assertFalse(state.isGoogleLoading)
        assertNull(state.pendingAuthIntent)
        assertNull(state.error)
        assertFalse(state.isCompleted)
    }

    @Test
    fun `GoogleAuthResult success creates local profile with parsed Google name and completes with autoOpenCloudHub true`() = runTest {
        io.mockk.mockkStatic(Uri::class)
        val mockParsedUri = mockk<Uri>()
        io.mockk.every { Uri.parse("https://lh3.googleusercontent.com/photo") } returns mockParsedUri

        val mockIntent = mockk<Intent>()
        val account = CloudAccount(
            accountId = "google-id-456",
            email = "carlos.mendez@estudiante.edu",
            displayName = "Carlos Méndez",
            photoUrl = "https://lh3.googleusercontent.com/photo"
        )
        coEvery {
            googleAccountRepository.completeAuthorization(mockIntent)
        } returns Result.success(account)
        coEvery {
            authRepository.createLocalProfile("Carlos", "Méndez", mockParsedUri)
        } returns Result.success("user-456")

        viewModel.onEvent(OnboardingEvent.GoogleAuthResult(mockIntent))

        val state = viewModel.uiState.value
        assertFalse(state.isGoogleLoading)
        assertTrue(state.isCompleted)
        assertTrue(state.autoOpenCloudHub)
        assertNull(state.error)

        coVerify(exactly = 1) {
            authRepository.createLocalProfile("Carlos", "Méndez", mockParsedUri)
        }

        io.mockk.unmockkStatic(Uri::class)
    }

    @Test
    fun `GoogleAuthResult failure with cancelled does not show error`() = runTest {
        val mockIntent = mockk<Intent>()
        coEvery {
            googleAccountRepository.completeAuthorization(mockIntent)
        } returns Result.failure(CloudAuthException(CloudAuthException.Reason.CANCELLED))

        viewModel.onEvent(OnboardingEvent.GoogleAuthResult(mockIntent))

        val state = viewModel.uiState.value
        assertFalse(state.isGoogleLoading)
        assertFalse(state.isCompleted)
        assertNull(state.error)
    }

    @Test
    fun `GoogleAuthResult failure with other error sets error message`() = runTest {
        val mockIntent = mockk<Intent>()
        coEvery {
            googleAccountRepository.completeAuthorization(mockIntent)
        } returns Result.failure(Exception("Error de red con Google Drive"))

        viewModel.onEvent(OnboardingEvent.GoogleAuthResult(mockIntent))

        val state = viewModel.uiState.value
        assertFalse(state.isGoogleLoading)
        assertFalse(state.isCompleted)
        assertEquals("Error de red con Google Drive.", state.error)
    }

    @Test
    fun `parseGoogleName correctly parses single, double, compound names and email fallback`() {
        val (first1, last1) = viewModel.parseGoogleName("Carlos", "carlos@gmail.com")
        assertEquals("Carlos", first1)
        assertEquals("", last1)

        val (first2, last2) = viewModel.parseGoogleName("Carlos Gómez", "carlos@gmail.com")
        assertEquals("Carlos", first2)
        assertEquals("Gómez", last2)

        val (first3, last3) = viewModel.parseGoogleName("María José de la Vega", "maria@gmail.com")
        assertEquals("María José de la", first3)
        assertEquals("Vega", last3)

        val (first4, last4) = viewModel.parseGoogleName(null, "estudiante123@gmail.com")
        assertEquals("estudiante123", first4)
        assertEquals("", last4)

        val (first5, last5) = viewModel.parseGoogleName("", "")
        assertEquals("Estudiante", first5)
        assertEquals("", last5)
    }
}
