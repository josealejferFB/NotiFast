package com.notifast.app.domain.usecase.cloud

import app.cash.turbine.test
import com.notifast.app.data.backup.ContentHasher
import com.notifast.app.data.backup.SubjectSnapshot
import com.notifast.app.data.local.dao.SubjectCloudStateDao
import com.notifast.app.data.local.entity.SubjectCloudStateEntity
import com.notifast.app.data.model.SubjectData
import com.notifast.app.domain.model.cloud.CloudAccount
import com.notifast.app.domain.model.cloud.LocalSyncSummary
import com.notifast.app.domain.repository.GoogleAccountRepository
import com.notifast.app.domain.repository.SubjectBackupStore
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ObserveLocalSyncSummaryUseCaseTest {

    private val accountRepository: GoogleAccountRepository = mockk()
    private val stateDao: SubjectCloudStateDao = mockk()
    private val store: SubjectBackupStore = mockk()
    private val hasher: ContentHasher = mockk()

    private lateinit var useCase: ObserveLocalSyncSummaryUseCase

    @Before
    fun setUp() {
        useCase = ObserveLocalSyncSummaryUseCase(accountRepository, stateDao, store, hasher)
    }

    private fun mockSnapshot(id: String, name: String): SubjectSnapshot {
        return SubjectSnapshot(
            subject = SubjectData(id = id, name = name, colorHex = "#BF092F", icon = "calculator"),
            imageFiles = emptyMap()
        )
    }

    @Test
    fun invoke_whenNoAccount_emitsNoAccount() = runTest {
        every { accountRepository.account } returns flowOf(null)

        useCase("u1").test {
            assertEquals(LocalSyncSummary.NO_ACCOUNT, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun invoke_whenAccountLinked_andSubjectMissingState_emitsPending() = runTest {
        val account = CloudAccount("acc1", "a@test.com", "Name", null)
        every { accountRepository.account } returns flowOf(account)
        every { stateDao.observe("acc1") } returns flowOf(emptyList())

        val snap = mockSnapshot("sub1", "Física")
        coEvery { store.listSubjectIds("u1") } returns listOf("sub1" to "Física")
        coEvery { store.readSnapshot("u1", "sub1") } returns Result.success(snap)
        every { hasher.hash(snap) } returns "hash1"

        useCase("u1").test {
            assertEquals(LocalSyncSummary.PENDING, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun invoke_whenAccountLinked_andHashMismatch_emitsPending() = runTest {
        val account = CloudAccount("acc1", "a@test.com", "Name", null)
        every { accountRepository.account } returns flowOf(account)
        val state = SubjectCloudStateEntity("acc1", "sub1", "f1", "hashSaved", 1000L)
        every { stateDao.observe("acc1") } returns flowOf(listOf(state))

        val snap = mockSnapshot("sub1", "Física")
        coEvery { store.listSubjectIds("u1") } returns listOf("sub1" to "Física")
        coEvery { store.readSnapshot("u1", "sub1") } returns Result.success(snap)
        every { hasher.hash(snap) } returns "hashModified"

        useCase("u1").test {
            assertEquals(LocalSyncSummary.PENDING, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun invoke_whenAccountLinked_andAllSubjectsSynced_emitsAllSynced() = runTest {
        val account = CloudAccount("acc1", "a@test.com", "Name", null)
        every { accountRepository.account } returns flowOf(account)
        val state = SubjectCloudStateEntity("acc1", "sub1", "f1", "hashExact", 1000L)
        every { stateDao.observe("acc1") } returns flowOf(listOf(state))

        val snap = mockSnapshot("sub1", "Física")
        coEvery { store.listSubjectIds("u1") } returns listOf("sub1" to "Física")
        coEvery { store.readSnapshot("u1", "sub1") } returns Result.success(snap)
        every { hasher.hash(snap) } returns "hashExact"

        useCase("u1").test {
            assertEquals(LocalSyncSummary.ALL_SYNCED, awaitItem())
            awaitComplete()
        }
    }
}
