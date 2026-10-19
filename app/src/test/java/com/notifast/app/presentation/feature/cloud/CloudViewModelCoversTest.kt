package com.notifast.app.presentation.feature.cloud

import android.content.Context
import com.notifast.app.domain.model.cloud.CloudAccount
import com.notifast.app.domain.model.cloud.CloudFile
import com.notifast.app.domain.model.cloud.CloudOverview
import com.notifast.app.domain.model.cloud.CloudSubjectStatus
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.GoogleAccountRepository
import com.notifast.app.domain.usecase.ResolveSubjectNameUseCase
import com.notifast.app.domain.usecase.cloud.BuildCloudOverviewUseCase
import com.notifast.app.domain.usecase.cloud.DeleteCloudSubjectUseCase
import com.notifast.app.domain.usecase.cloud.RestoreCoversUseCase
import com.notifast.app.domain.usecase.cloud.RestoreGlobalDataUseCase
import com.notifast.app.domain.usecase.cloud.RestoreSubjectsUseCase
import com.notifast.app.domain.usecase.cloud.UploadCoversUseCase
import com.notifast.app.domain.usecase.cloud.UploadGlobalDataUseCase
import com.notifast.app.domain.usecase.cloud.UploadSubjectsUseCase
import com.notifast.app.domain.util.NetworkMonitor
import com.notifast.app.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class CloudViewModelCoversTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val googleAccountRepository: GoogleAccountRepository = mockk(relaxed = true)
    private val buildCloudOverviewUseCase: BuildCloudOverviewUseCase = mockk(relaxed = true)
    private val uploadSubjectsUseCase: UploadSubjectsUseCase = mockk(relaxed = true)
    private val uploadGlobalDataUseCase: UploadGlobalDataUseCase = mockk(relaxed = true)
    private val uploadCoversUseCase: UploadCoversUseCase = mockk(relaxed = true)
    private val restoreSubjectsUseCase: RestoreSubjectsUseCase = mockk(relaxed = true)
    private val restoreGlobalDataUseCase: RestoreGlobalDataUseCase = mockk(relaxed = true)
    private val restoreCoversUseCase: RestoreCoversUseCase = mockk(relaxed = true)
    private val deleteCloudSubjectUseCase: DeleteCloudSubjectUseCase = mockk(relaxed = true)
    private val resolveSubjectNameUseCase: ResolveSubjectNameUseCase = ResolveSubjectNameUseCase()
    private val networkMonitor: NetworkMonitor = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)

    private val accountFlow = MutableStateFlow<CloudAccount?>(null)
    private val isOnlineFlow = MutableStateFlow(true)
    private val tempDir = File(System.getProperty("java.io.tmpdir") ?: "/tmp", "cloud_vm_covers_test")

    private val testAccount = CloudAccount(
        accountId = "google_user_covers",
        email = "user@test.com",
        displayName = "Test User",
        photoUrl = null
    )

    private val dummyCoversFile = CloudFile(
        driveFileId = "df_covers_1",
        kind = "covers",
        subjectId = null,
        subjectName = "Portadas de Estudio",
        icon = "📄",
        colorHex = null,
        contentHash = "hash_cloud_covers",
        uploadedAt = 1000L,
        deviceModel = "Pixel",
        noteCount = 0,
        evaluationCount = 0,
        coverCount = 2,
        sizeBytes = 1024L,
        schemaVersion = 1
    )

    @Before
    fun setUp() {
        tempDir.mkdirs()
        every { context.cacheDir } returns tempDir
        every { googleAccountRepository.account } returns accountFlow
        every { networkMonitor.isOnline } returns isOnlineFlow
        every { networkMonitor.isMetered() } returns false
        coEvery { authRepository.getCurrentUserId() } returns "user_123"

        coEvery { buildCloudOverviewUseCase("user_123") } returns Result.success(
            CloudOverview(
                subjects = emptyList(),
                global = null,
                covers = dummyCoversFile,
                localCoverCount = 2,
                coversStatus = CloudSubjectStatus.LOCAL_CHANGES
            )
        )
    }

    private fun createViewModel(): CloudViewModel {
        return CloudViewModel(
            googleAccountRepository = googleAccountRepository,
            buildCloudOverviewUseCase = buildCloudOverviewUseCase,
            uploadSubjectsUseCase = uploadSubjectsUseCase,
            uploadGlobalDataUseCase = uploadGlobalDataUseCase,
            uploadCoversUseCase = uploadCoversUseCase,
            restoreSubjectsUseCase = restoreSubjectsUseCase,
            restoreGlobalDataUseCase = restoreGlobalDataUseCase,
            restoreCoversUseCase = restoreCoversUseCase,
            deleteCloudSubjectUseCase = deleteCloudSubjectUseCase,
            resolveSubjectNameUseCase = resolveSubjectNameUseCase,
            networkMonitor = networkMonitor,
            authRepository = authRepository,
            context = context
        )
    }

    @Test
    fun toggleCovers_togglesSelectionAndRecalculatesCanUploadAndCanRestore() = runTest {
        val viewModel = createViewModel()
        accountFlow.value = testAccount
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.coversSelected)
        assertFalse(viewModel.uiState.value.canUpload)
        assertFalse(viewModel.uiState.value.canRestore)

        viewModel.onEvent(CloudEvent.ToggleCovers)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.coversSelected)
        // Since coversStatus is LOCAL_CHANGES and covers != null, both canUpload and canRestore become true
        assertTrue(viewModel.uiState.value.canUpload)
        assertTrue(viewModel.uiState.value.canRestore)

        viewModel.onEvent(CloudEvent.ToggleCovers)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.coversSelected)
        assertFalse(viewModel.uiState.value.canUpload)
    }

    @Test
    fun upload_whenCoversSelected_executesUploadCoversUseCaseAndEmitsOutcome() = runTest {
        coEvery { uploadCoversUseCase("user_123", dummyCoversFile, any()) } returns Result.success(Unit)

        val viewModel = createViewModel()
        accountFlow.value = testAccount
        advanceUntilIdle()

        viewModel.onEvent(CloudEvent.ToggleCovers)
        advanceUntilIdle()

        viewModel.onEvent(CloudEvent.Upload)
        advanceUntilIdle()

        coVerify(exactly = 1) { uploadCoversUseCase("user_123", dummyCoversFile, any()) }

        val summary = viewModel.uiState.value.summary
        assertNotNull(summary)
        val coverOutcome = summary?.outcomes?.firstOrNull { it.id == "covers" && it.error == null }
        assertNotNull(coverOutcome)
        assertEquals("Portadas de Estudio", coverOutcome?.name)
    }

    @Test
    fun restore_whenCoversSelected_executesRestoreCoversUseCaseAndEmitsOutcome() = runTest {
        coEvery { restoreCoversUseCase("user_123", dummyCoversFile, any()) } returns Result.success(Unit)

        val viewModel = createViewModel()
        accountFlow.value = testAccount
        advanceUntilIdle()

        viewModel.onEvent(CloudEvent.ToggleCovers)
        advanceUntilIdle()

        viewModel.onEvent(CloudEvent.Restore)
        advanceUntilIdle()

        assertEquals(CloudConfirm.RestoreCovers, viewModel.uiState.value.pendingConfirm)

        viewModel.onEvent(CloudEvent.Confirm(true))
        advanceUntilIdle()

        coVerify(exactly = 1) { restoreCoversUseCase("user_123", dummyCoversFile, any()) }

        val summary = viewModel.uiState.value.summary
        assertNotNull(summary)
        val coverOutcome = summary?.outcomes?.firstOrNull { it.id == "covers" && it.error == null }
        assertNotNull(coverOutcome)
        assertEquals("Portadas de Estudio", coverOutcome?.name)
    }
}
