package com.notifast.app.domain.repository

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.notifast.app.domain.model.study.CoverDraft

sealed interface GoogleDocsExportStep {
    data class Exported(
        val googleDocId: String,
        val webViewUrl: String,
        val updatedDraft: CoverDraft
    ) : GoogleDocsExportStep

    data class NeedsResolution(
        val pendingIntent: PendingIntent
    ) : GoogleDocsExportStep
}

interface GoogleDocsExportRepository {
    suspend fun exportDraftToGoogleDocs(draft: CoverDraft, activityContext: Context? = null): Result<GoogleDocsExportStep>
    suspend fun completeExportAfterResolution(intent: Intent?, draft: CoverDraft): Result<GoogleDocsExportStep>
}
