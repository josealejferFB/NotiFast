package com.notifast.app.data.repository

import android.app.PendingIntent
import com.notifast.app.data.remote.drive.CreatedGoogleDocDto
import com.notifast.app.data.remote.drive.DriveRestClient
import com.notifast.app.data.remote.google.DriveAuthResult
import com.notifast.app.data.remote.google.GoogleAuthDataSource
import com.notifast.app.data.study.OpenXmlDocxGenerator
import com.notifast.app.domain.model.cloud.CloudAccount
import com.notifast.app.domain.model.study.CoverDraft
import com.notifast.app.domain.model.study.CoverTemplateType
import com.notifast.app.domain.repository.GoogleAccountRepository
import com.notifast.app.domain.repository.GoogleDocsExportRepository
import com.notifast.app.domain.repository.GoogleDocsExportStep
import com.notifast.app.domain.repository.StudyDocumentRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class GoogleDocsExportRepositoryImplTest {

    private lateinit var authDataSource: GoogleAuthDataSource
    private lateinit var accountRepository: GoogleAccountRepository
    private lateinit var driveClient: DriveRestClient
    private lateinit var docxGenerator: OpenXmlDocxGenerator
    private lateinit var studyDocRepository: StudyDocumentRepository
    private lateinit var repository: GoogleDocsExportRepository

    @Before
    fun setUp() {
        authDataSource = mockk(relaxed = true)
        accountRepository = mockk(relaxed = true)
        driveClient = mockk(relaxed = true)
        docxGenerator = OpenXmlDocxGenerator()
        studyDocRepository = mockk(relaxed = true)

        repository = GoogleDocsExportRepositoryImpl(
            authDataSource = authDataSource,
            accountRepository = accountRepository,
            driveClient = driveClient,
            docxGenerator = docxGenerator,
            studyDocRepository = studyDocRepository
        )
    }

    @Test
    fun `exportDraftToGoogleDocs returns NeedsResolution when authorizeDriveFile requires user consent`() = runTest {
        val account = CloudAccount(accountId = "acc-1", email = "test@gmail.com", displayName = "Test", photoUrl = null)
        coEvery { accountRepository.account } returns flowOf(account)

        val pendingIntent: PendingIntent = mockk()
        coEvery { authDataSource.authorizeDriveFile("test@gmail.com") } returns DriveAuthResult.NeedsResolution(pendingIntent)

        val draft = CoverDraft(id = "d1", userId = "u1", templateType = CoverTemplateType.APA_7, workTitle = "Portada APA")
        val result = repository.exportDraftToGoogleDocs(draft, null)

        assertTrue(result.isSuccess)
        val step = result.getOrThrow()
        assertTrue(step is GoogleDocsExportStep.NeedsResolution)
        assertEquals(pendingIntent, (step as GoogleDocsExportStep.NeedsResolution).pendingIntent)
    }

    @Test
    fun `exportDraftToGoogleDocs uploads HTML and updates draft with googleDocId`() = runTest {
        val account = CloudAccount(accountId = "acc-1", email = "test@gmail.com", displayName = "Test", photoUrl = null)
        coEvery { accountRepository.account } returns flowOf(account)

        coEvery { authDataSource.authorizeDriveFile("test@gmail.com") } returns DriveAuthResult.Success("valid-file-token")
        coEvery { driveClient.createGoogleDocFromDocx("valid-file-token", any(), any()) } returns CreatedGoogleDocDto(
            id = "doc-12345",
            name = "Portada APA",
            webViewLink = "https://docs.google.com/document/d/doc-12345/edit"
        )
        coEvery { driveClient.createGoogleDocFromHtml("valid-file-token", any(), any()) } returns CreatedGoogleDocDto(
            id = "doc-12345",
            name = "Portada APA",
            webViewLink = "https://docs.google.com/document/d/doc-12345/edit"
        )

        val draft = CoverDraft(
            id = "d1",
            userId = "u1",
            templateType = CoverTemplateType.APA_7,
            workTitle = "Portada APA",
            generatedLocalDocId = "local-doc-1"
        )

        val result = repository.exportDraftToGoogleDocs(draft, null)
        assertTrue(result.isSuccess)
        val step = result.getOrThrow()
        assertTrue(step is GoogleDocsExportStep.Exported)
        val exported = step as GoogleDocsExportStep.Exported
        assertEquals("doc-12345", exported.googleDocId)
        assertEquals("https://docs.google.com/document/d/doc-12345/edit", exported.webViewUrl)

        coVerify { studyDocRepository.saveCoverDraft(match { it.googleDocId == "doc-12345" }) }
        coVerify { studyDocRepository.updateGoogleDocLink("u1", "local-doc-1", "doc-12345", "https://docs.google.com/document/d/doc-12345/edit") }
    }
}
