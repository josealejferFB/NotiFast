package com.notifast.app.presentation.feature.cloud

import android.content.Context
import com.notifast.app.domain.model.cloud.BatchResult
import com.notifast.app.domain.model.cloud.CloudAccount
import com.notifast.app.domain.model.cloud.CloudFile
import com.notifast.app.domain.model.cloud.CloudOverview
import com.notifast.app.domain.model.cloud.CloudSubjectItem
import com.notifast.app.domain.model.cloud.CloudSubjectStatus
import com.notifast.app.domain.model.cloud.ItemOutcome
import com.notifast.app.domain.model.cloud.RestoreConflict
import com.notifast.app.domain.model.cloud.RestoreDecision
import com.notifast.app.domain.model.cloud.UploadProgress
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.GoogleAccountRepository
import com.notifast.app.domain.usecase.ResolveSubjectNameUseCase
import com.notifast.app.domain.usecase.cloud.BuildCloudOverviewUseCase
import com.notifast.app.domain.usecase.cloud.DeleteCloudSubjectUseCase
import com.notifast.app.domain.usecase.cloud.RestoreGlobalDataUseCase
import com.notifast.app.domain.usecase.cloud.RestoreSubjectsUseCase
import com.notifast.app.domain.usecase.cloud.UploadGlobalDataUseCase
import com.notifast.app.domain.usecase.cloud.UploadSubjectsUseCase
import com.notifast.app.domain.util.NetworkMonitor
import com.notifast.app.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class CloudViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val googleAccountRepository: GoogleAccountRepository = mockk(relaxed = true)
    private val buildCloudOverviewUseCase: BuildCloudOverviewUseCase = mockk(relaxed = true)
    private val uploadSubjectsUseCase: UploadSubjectsUseCase = mockk(relaxed = true)
    private val uploadGlobalDataUseCase: UploadGlobalDataUseCase = mockk(relaxed = true)
    private val restoreSubjectsUseCase: RestoreSubjectsUseCase = mockk(relaxed = true)
    private val restoreGlobalDataUseCase: RestoreGlobalDataUseCase = mockk(relaxed = true)
    private val deleteCloudSubjectUseCase: DeleteCloudSubjectUseCase = mockk(relaxed = true)
    private val resolveSubjectNameUseCase: ResolveSubjectNameUseCase = ResolveSubjectNameUseCase()
    private val networkMonitor: NetworkMonitor = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val context: Context = mockk(relaxed = true)

    private val accountFlow = MutableStateFlow<CloudAccount?>(null)
    private val isOnlineFlow = MutableStateFlow(true)
    private val tempDir = File(System.getProperty("java.io.tmpdir") ?: "/tmp", "cloud_vm_test")

    private val testAccount = CloudAccount(
        accountId = "google_user_1",
        email = "test@example.com",
        displayName = "Test User",
        photoUrl = null
    )

    private val dummyCloudFile = CloudFile(
        driveFileId = "df_1",
        kind = "subject",
        subjectId = "sub_1",
        subjectName = "Cálculo",
        icon = "Calculate",
        colorHex = "#BF092F",
        contentHash = "hash1",
        uploadedAt = 1000L,
        deviceModel = "Pixel",
        noteCount = 5,
        evaluationCount = 2,
        sizeBytes = 1024L,
        schemaVersion = 1
    )

    private val sampleItems = listOf(
        CloudSubjectItem(
            subjectId = "sub_local",
            name = "Local Solo",
            icon = "Book",
            colorHex = "#123456",
            status = CloudSubjectStatus.LOCAL_ONLY,
            driveFileId = null,
            localHash = "h_local",
            cloudFile = null
        ),
        CloudSubjectItem(
            subjectId = "sub_cloud",
            name = "Cloud Solo",
            icon = "Cloud",
            colorHex = "#654321",
            status = CloudSubjectStatus.CLOUD_ONLY,
            driveFileId = "df_cloud",
            localHash = null,
            cloudFile = dummyCloudFile.copy(subjectId = "sub_cloud", driveFileId = "df_cloud")
        ),
        CloudSubjectItem(
            subjectId = "sub_synced",
            name = "Synced",
            icon = "Check",
            colorHex = "#00FF00",
            status = CloudSubjectStatus.SYNCED,
            driveFileId = "df_synced",
            localHash = "h_sync",
            cloudFile = dummyCloudFile.copy(subjectId = "sub_synced", driveFileId = "df_synced")
        ),
        CloudSubjectItem(
            subjectId = "sub_changes",
            name = "Local Changes",
            icon = "Edit",
            colorHex = "#FFAA00",
            status = CloudSubjectStatus.LOCAL_CHANGES,
            driveFileId = "df_changes",
            localHash = "h_new",
            cloudFile = dummyCloudFile.copy(subjectId = "sub_changes", driveFileId = "df_changes")
        ),
        CloudSubjectItem(
            subjectId = "sub_newer",
            name = "Cloud Newer",
            icon = "Warning",
            colorHex = "#FF0000",
            status = CloudSubjectStatus.CLOUD_NEWER,
            driveFileId = "df_newer",
            localHash = "h_old",
            cloudFile = dummyCloudFile.copy(subjectId = "sub_newer", driveFileId = "df_newer")
        )
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
                subjects = sampleItems,
                global = dummyCloudFile.copy(kind = "global", subjectId = null)
            )
        )
    }

    private fun createViewModel(): CloudViewModel {
        return CloudViewModel(
            googleAccountRepository = googleAccountRepository,
            buildCloudOverviewUseCase = buildCloudOverviewUseCase,
            uploadSubjectsUseCase = uploadSubjectsUseCase,
            uploadGlobalDataUseCase = uploadGlobalDataUseCase,
            restoreSubjectsUseCase = restoreSubjectsUseCase,
            restoreGlobalDataUseCase = restoreGlobalDataUseCase,
            deleteCloudSubjectUseCase = deleteCloudSubjectUseCase,
            resolveSubjectNameUseCase = resolveSubjectNameUseCase,
            networkMonitor = networkMonitor,
            authRepository = authRepository,
            context = context
        )
    }

    @Test
    fun `selection logic updates canUpload and canRestore flags per status`() = runTest {
        accountFlow.value = testAccount
        val vm = createViewModel()
        advanceUntilIdle()

        assertEquals(5, vm.uiState.value.items.size)
        assertEquals(1, vm.uiState.value.cloudOnlyBanner)

        // Select LOCAL_ONLY item: can upload, cannot restore
        vm.onEvent(CloudEvent.Toggle("sub_local"))
        assertTrue(vm.uiState.value.canUpload)
        assertFalse(vm.uiState.value.canRestore)

        // Unselect LOCAL_ONLY and select CLOUD_ONLY: cannot upload, can restore
        vm.onEvent(CloudEvent.Toggle("sub_local"))
        vm.onEvent(CloudEvent.Toggle("sub_cloud"))
        assertFalse(vm.uiState.value.canUpload)
        assertTrue(vm.uiState.value.canRestore)

        // Unselect CLOUD_ONLY and select SYNCED: cannot upload, can restore
        vm.onEvent(CloudEvent.Toggle("sub_cloud"))
        vm.onEvent(CloudEvent.Toggle("sub_synced"))
        assertFalse(vm.uiState.value.canUpload)
        assertTrue(vm.uiState.value.canRestore)

        // Select LOCAL_CHANGES: can upload and can restore
        vm.onEvent(CloudEvent.Toggle("sub_changes"))
        assertTrue(vm.uiState.value.canUpload)
        assertTrue(vm.uiState.value.canRestore)

        // SelectAll selects all items
        vm.onEvent(CloudEvent.SelectAll)
        assertEquals(5, vm.uiState.value.selected.size)
        assertTrue(vm.uiState.value.canUpload)
        assertTrue(vm.uiState.value.canRestore)

        // SelectAll again deselects all
        vm.onEvent(CloudEvent.SelectAll)
        assertTrue(vm.uiState.value.selected.isEmpty())
        assertFalse(vm.uiState.value.canUpload)
        assertFalse(vm.uiState.value.canRestore)

        // ToggleGlobal enables upload and restore
        vm.onEvent(CloudEvent.ToggleGlobal)
        assertTrue(vm.uiState.value.globalSelected)
        assertTrue(vm.uiState.value.canUpload)
        assertTrue(vm.uiState.value.canRestore)
    }

    @Test
    fun `CLOUD_NEWER upload sets pendingConfirm and Confirm false cancels it`() = runTest {
        accountFlow.value = testAccount
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onEvent(CloudEvent.Toggle("sub_newer"))
        vm.onEvent(CloudEvent.Upload)

        assertEquals(CloudConfirm.CloudNewer, vm.uiState.value.pendingConfirm)

        vm.onEvent(CloudEvent.Confirm(accepted = false))
        assertNull(vm.uiState.value.pendingConfirm)
        coVerify(exactly = 0) { uploadSubjectsUseCase(any(), any(), any()) }
    }

    @Test
    fun `metered network over 20MB prompts MobileDataLarge and Confirm true proceeds`() = runTest {
        accountFlow.value = testAccount
        every { networkMonitor.isMetered() } returns true
        coEvery { uploadSubjectsUseCase.estimateBytes(any(), any()) } returns 25 * 1024 * 1024L
        coEvery { uploadSubjectsUseCase(any(), any(), any()) } returns flowOf(
            UploadProgress.Done(BatchResult(emptyList()))
        )

        val vm = createViewModel()
        advanceUntilIdle()

        vm.onEvent(CloudEvent.Toggle("sub_changes"))
        vm.onEvent(CloudEvent.Upload)
        advanceUntilIdle()

        val confirm = vm.uiState.value.pendingConfirm
        assertTrue(confirm is CloudConfirm.MobileDataLarge)
        assertEquals(25 * 1024 * 1024L, (confirm as CloudConfirm.MobileDataLarge).bytes)

        vm.onEvent(CloudEvent.Confirm(accepted = true))
        advanceUntilIdle()

        assertNull(vm.uiState.value.pendingConfirm)
        coVerify(atLeast = 1) { uploadSubjectsUseCase("user_123", any(), any()) }
    }

    @Test
    fun `offline state disables actions and sets isOffline`() = runTest {
        accountFlow.value = testAccount
        isOnlineFlow.value = false
        val vm = createViewModel()
        advanceUntilIdle()

        assertTrue(vm.uiState.value.isOffline)

        vm.onEvent(CloudEvent.Toggle("sub_local"))
        vm.onEvent(CloudEvent.Upload)
        advanceUntilIdle()

        assertEquals(CloudError.OFFLINE, vm.uiState.value.error)
        coVerify(exactly = 0) { uploadSubjectsUseCase(any(), any(), any()) }
    }

    @Test
    fun `account switch reloads overview and clears old selection`() = runTest {
        accountFlow.value = testAccount
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onEvent(CloudEvent.Toggle("sub_local"))
        assertEquals(1, vm.uiState.value.selected.size)

        // Account switches to null
        accountFlow.value = null
        advanceUntilIdle()

        assertNull(vm.uiState.value.account)
        assertTrue(vm.uiState.value.items.isEmpty())
        assertTrue(vm.uiState.value.selected.isEmpty())

        // Account switches to a new account
        val newAccount = testAccount.copy(accountId = "google_user_2")
        accountFlow.value = newAccount
        advanceUntilIdle()

        assertEquals(newAccount, vm.uiState.value.account)
        assertEquals(5, vm.uiState.value.items.size)
        assertTrue(vm.uiState.value.selected.isEmpty())
    }

    @Test
    fun `restore detects conflicts and queues into pendingConflicts, applyToRest resolves remainder`() = runTest {
        accountFlow.value = testAccount
        val conflict1 = RestoreConflict(
            item = sampleItems[1],
            localSubjectId = "sub_local_1",
            localName = "Cloud Solo",
            hasLocalChanges = true,
            suggestedName = "Cloud Solo (1)"
        )
        val conflict2 = RestoreConflict(
            item = sampleItems[2],
            localSubjectId = "sub_local_2",
            localName = "Synced",
            hasLocalChanges = false,
            suggestedName = "Synced (1)"
        )
        coEvery { restoreSubjectsUseCase.findConflicts(any(), any()) } returns listOf(conflict1, conflict2)
        coEvery { restoreSubjectsUseCase(any(), any(), any(), any()) } returns flowOf(
            UploadProgress.Done(BatchResult(emptyList()))
        )

        val vm = createViewModel()
        advanceUntilIdle()

        vm.onEvent(CloudEvent.Toggle("sub_cloud"))
        vm.onEvent(CloudEvent.Toggle("sub_synced"))
        vm.onEvent(CloudEvent.Restore)
        advanceUntilIdle()

        assertEquals(2, vm.uiState.value.pendingConflicts.size)

        // Resolve first with applyToRest = true
        vm.onEvent(CloudEvent.ResolveConflict(RestoreDecision.Overwrite, applyToRest = true))
        advanceUntilIdle()

        assertTrue(vm.uiState.value.pendingConflicts.isEmpty())
        coVerify {
            restoreSubjectsUseCase(
                "user_123",
                any(),
                match { decisions ->
                    decisions[conflict1.item.subjectId] == RestoreDecision.Overwrite &&
                            decisions[conflict2.item.subjectId] == RestoreDecision.Overwrite
                },
                any()
            )
        }
    }

    @Test
    fun `summary shows failures and RetryFailed re-runs only failed items`() = runTest {
        accountFlow.value = testAccount
        coEvery { uploadSubjectsUseCase(any(), any(), any()) } returns flowOf(
            UploadProgress.Done(
                BatchResult(
                    listOf(
                        ItemOutcome("sub_local", "Local Solo", null),
                        ItemOutcome("sub_changes", "Local Changes", Exception("Network error"))
                    )
                )
            )
        )

        val vm = createViewModel()
        advanceUntilIdle()

        vm.onEvent(CloudEvent.Toggle("sub_local"))
        vm.onEvent(CloudEvent.Toggle("sub_changes"))
        vm.onEvent(CloudEvent.Upload)
        advanceUntilIdle()

        assertNotNull(vm.uiState.value.summary)
        assertEquals(1, vm.uiState.value.summary?.failed?.size)

        // RetryFailed should re-run only sub_changes
        vm.onEvent(CloudEvent.RetryFailed)
        advanceUntilIdle()

        coVerify {
            uploadSubjectsUseCase(
                "user_123",
                match { list -> list.size == 1 && list[0].subjectId == "sub_changes" },
                any()
            )
        }
    }

    @Test
    fun `candidate conflict name validation updates conflictNameError`() = runTest {
        accountFlow.value = testAccount
        val vm = createViewModel()
        advanceUntilIdle()

        vm.onEvent(CloudEvent.ValidateConflictName(""))
        assertNotNull(vm.uiState.value.conflictNameError)

        vm.onEvent(CloudEvent.ValidateConflictName("Local Solo"))
        assertNotNull(vm.uiState.value.conflictNameError)

        vm.onEvent(CloudEvent.ValidateConflictName("Valid Unique Name"))
        assertNull(vm.uiState.value.conflictNameError)
    }

    @Test
    fun `DeleteFromCloud sets pendingConfirm and on Confirm true calls deleteCloudSubjectUseCase`() = runTest {
        accountFlow.value = testAccount
        coEvery { deleteCloudSubjectUseCase.cloudOnly("df_cloud") } returns Result.success(Unit)

        val vm = createViewModel()
        advanceUntilIdle()

        vm.onEvent(CloudEvent.DeleteFromCloud("sub_cloud"))
        val confirm = vm.uiState.value.pendingConfirm
        assertTrue(confirm is CloudConfirm.DeleteCloud)
        assertEquals("sub_cloud", (confirm as CloudConfirm.DeleteCloud).item.subjectId)

        vm.onEvent(CloudEvent.Confirm(accepted = true))
        advanceUntilIdle()

        assertNull(vm.uiState.value.pendingConfirm)
        coVerify { deleteCloudSubjectUseCase.cloudOnly("df_cloud") }
        coVerify(atLeast = 2) { buildCloudOverviewUseCase("user_123") }
    }

    @Test
    fun `failed upload populates failedSubjectIds and Refresh or Toggle clears them`() = runTest {
        accountFlow.value = testAccount
        coEvery { uploadSubjectsUseCase(any(), any(), any()) } returns flowOf(
            UploadProgress.Done(
                BatchResult(
                    listOf(
                        ItemOutcome("sub_local", "Local Solo", null),
                        ItemOutcome("sub_changes", "Local Changes", Exception("Transient error"))
                    )
                )
            )
        )

        val vm = createViewModel()
        advanceUntilIdle()

        vm.onEvent(CloudEvent.Toggle("sub_local"))
        vm.onEvent(CloudEvent.Toggle("sub_changes"))
        vm.onEvent(CloudEvent.Upload)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.failedSubjectIds.contains("sub_changes"))
        assertNotNull(vm.uiState.value.summary)

        // Toggling sub_changes should clear its failure state
        vm.onEvent(CloudEvent.Toggle("sub_changes"))
        assertFalse(vm.uiState.value.failedSubjectIds.contains("sub_changes"))
        assertNull(vm.uiState.value.summary)

        // Re-inject error state manually to test Refresh
        vm.onEvent(CloudEvent.Upload)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.failedSubjectIds.contains("sub_changes"))

        // Refresh clears failedSubjectIds and summary
        vm.onEvent(CloudEvent.Refresh)
        advanceUntilIdle()
        assertTrue(vm.uiState.value.failedSubjectIds.isEmpty())
        assertNull(vm.uiState.value.summary)
    }

    @Test
    fun `DismissSummary clears summary and failedSubjectIds`() = runTest {
        accountFlow.value = testAccount
        coEvery { uploadSubjectsUseCase(any(), any(), any()) } returns flowOf(
            UploadProgress.Done(
                BatchResult(
                    listOf(
                        ItemOutcome("sub_changes", "Local Changes", Exception("Network glitch"))
                    )
                )
            )
        )

        val vm = createViewModel()
        advanceUntilIdle()

        vm.onEvent(CloudEvent.Toggle("sub_changes"))
        vm.onEvent(CloudEvent.Upload)
        advanceUntilIdle()

        assertTrue(vm.uiState.value.failedSubjectIds.contains("sub_changes"))
        assertNotNull(vm.uiState.value.summary)

        vm.onEvent(CloudEvent.DismissSummary)
        assertNull(vm.uiState.value.summary)
        assertTrue(vm.uiState.value.failedSubjectIds.isEmpty())
    }
}
