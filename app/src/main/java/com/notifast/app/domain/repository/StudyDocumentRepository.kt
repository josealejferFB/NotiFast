package com.notifast.app.domain.repository

import android.net.Uri
import com.notifast.app.domain.model.study.CoverDraft
import com.notifast.app.domain.model.study.DocxPreviewBlock
import com.notifast.app.domain.model.study.StudyDocument
import kotlinx.coroutines.flow.Flow
import java.io.File

interface StudyDocumentRepository {
    fun observeDocuments(userId: String): Flow<List<StudyDocument>>
    fun observeDocumentsBySubject(userId: String, subjectName: String): Flow<List<StudyDocument>>
    suspend fun getDocumentById(userId: String, documentId: String): StudyDocument?
    suspend fun importDocument(userId: String, uri: Uri, subjectId: String, subjectName: String): Result<StudyDocument>
    suspend fun duplicateDocument(userId: String, documentId: String): Result<StudyDocument>
    suspend fun assignSubject(userId: String, documentId: String, subjectId: String, subjectName: String): Result<Unit>
    suspend fun updateGoogleDocLink(userId: String, documentId: String, googleDocId: String, webViewLink: String): Result<Unit>
    suspend fun deleteDocument(userId: String, documentId: String): Result<Unit>
    fun getDocumentFile(fileName: String): File
    suspend fun parseDocxPreview(fileName: String): Result<List<DocxPreviewBlock>>

    fun observeCoverDrafts(userId: String): Flow<List<CoverDraft>>
    suspend fun getLatestCoverDraft(userId: String): CoverDraft?
    suspend fun getCoverDraftById(userId: String, draftId: String): CoverDraft?
    suspend fun saveCoverDraft(draft: CoverDraft): Result<CoverDraft>
    suspend fun deleteCoverDraft(userId: String, draftId: String): Result<Unit>
    suspend fun generateLocalDocxFromDraft(draft: CoverDraft): Result<StudyDocument>
}
