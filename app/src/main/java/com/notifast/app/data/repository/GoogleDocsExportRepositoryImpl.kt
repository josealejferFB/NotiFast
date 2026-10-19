package com.notifast.app.data.repository

import android.content.Context
import android.content.Intent
import com.notifast.app.data.remote.drive.DriveRestClient
import com.notifast.app.data.remote.google.DriveAuthResult
import com.notifast.app.data.remote.google.GoogleAuthDataSource
import com.notifast.app.data.study.OpenXmlDocxGenerator
import com.notifast.app.domain.model.study.CoverDraft
import com.notifast.app.domain.repository.CloudAuthException
import com.notifast.app.domain.repository.GoogleAccountRepository
import com.notifast.app.domain.repository.GoogleDocsExportRepository
import com.notifast.app.domain.repository.GoogleDocsExportStep
import com.notifast.app.domain.repository.StudyDocumentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleDocsExportRepositoryImpl @Inject constructor(
    private val authDataSource: GoogleAuthDataSource,
    private val accountRepository: GoogleAccountRepository,
    private val driveClient: DriveRestClient,
    private val docxGenerator: OpenXmlDocxGenerator,
    private val studyDocRepository: StudyDocumentRepository
) : GoogleDocsExportRepository {

    override suspend fun exportDraftToGoogleDocs(
        draft: CoverDraft,
        activityContext: Context?
    ): Result<GoogleDocsExportStep> = withContext(Dispatchers.IO) {
        try {
            var currentAccount = accountRepository.account.first()

            if (currentAccount == null && activityContext != null) {
                val signInResult = accountRepository.signIn(activityContext).getOrElse {
                    return@withContext Result.failure(it)
                }
                when (signInResult) {
                    is com.notifast.app.domain.repository.AuthStep.Linked -> {
                        currentAccount = signInResult.account
                    }
                    is com.notifast.app.domain.repository.AuthStep.NeedsResolution -> {
                        return@withContext Result.success(GoogleDocsExportStep.NeedsResolution(signInResult.pendingIntent))
                    }
                }
            }

            val email = currentAccount?.email
                ?: return@withContext Result.failure(CloudAuthException(CloudAuthException.Reason.REAUTH_REQUIRED))

            when (val authResult = authDataSource.authorizeDriveFile(email)) {
                is DriveAuthResult.NeedsResolution -> {
                    Result.success(GoogleDocsExportStep.NeedsResolution(authResult.pendingIntent))
                }
                is DriveAuthResult.Success -> {
                    uploadAndPersist(authResult.accessToken, draft)
                }
                is DriveAuthResult.Failure -> {
                    Result.failure(authResult.throwable)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun completeExportAfterResolution(
        intent: Intent?,
        draft: CoverDraft
    ): Result<GoogleDocsExportStep> = withContext(Dispatchers.IO) {
        try {
            if (intent == null) {
                return@withContext Result.failure(CloudAuthException(CloudAuthException.Reason.CANCELLED))
            }

            when (val authResult = authDataSource.getAuthorizationResultFromIntent(intent)) {
                is DriveAuthResult.Success -> {
                    uploadAndPersist(authResult.accessToken, draft)
                }
                is DriveAuthResult.NeedsResolution -> {
                    Result.success(GoogleDocsExportStep.NeedsResolution(authResult.pendingIntent))
                }
                is DriveAuthResult.Failure -> {
                    Result.failure(authResult.throwable)
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun uploadAndPersist(
        token: String,
        draft: CoverDraft
    ): Result<GoogleDocsExportStep> {
        return try {
            val title = draft.workTitle.ifBlank { "Portada ${draft.templateType.name}" }

            val created = try {
                val baos = java.io.ByteArrayOutputStream()
                val genResult = docxGenerator.generateDocx(draft, baos)
                if (genResult.isSuccess && baos.size() > 0) {
                    driveClient.createGoogleDocFromDocx(token, title, baos.toByteArray())
                } else {
                    val htmlContent = docxGenerator.generateSemanticHtml(draft)
                    driveClient.createGoogleDocFromHtml(token, title, htmlContent)
                }
            } catch (_: Exception) {
                val htmlContent = docxGenerator.generateSemanticHtml(draft)
                driveClient.createGoogleDocFromHtml(token, title, htmlContent)
            }

            val webViewUrl = created.webViewLink ?: "https://docs.google.com/document/d/${created.id}/edit"

            val updatedDraft = draft.copy(
                googleDocId = created.id,
                googleWebViewLink = webViewUrl,
                updatedAt = System.currentTimeMillis()
            )
            studyDocRepository.saveCoverDraft(updatedDraft)

            if (draft.generatedLocalDocId != null) {
                studyDocRepository.updateGoogleDocLink(
                    userId = draft.userId,
                    documentId = draft.generatedLocalDocId,
                    googleDocId = created.id,
                    webViewLink = webViewUrl
                )
            }

            Result.success(
                GoogleDocsExportStep.Exported(
                    googleDocId = created.id,
                    webViewUrl = webViewUrl,
                    updatedDraft = updatedDraft
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
