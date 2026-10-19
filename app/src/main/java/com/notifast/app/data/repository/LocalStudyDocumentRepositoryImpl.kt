package com.notifast.app.data.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.notifast.app.data.local.dao.CoverDraftDao
import com.notifast.app.data.local.dao.StudyDocumentDao
import com.notifast.app.data.local.entity.CoverDraftEntity
import com.notifast.app.data.local.entity.StudyDocumentEntity
import com.notifast.app.data.study.OpenXmlDocxGenerator
import com.notifast.app.data.study.OpenXmlDocxParser
import com.notifast.app.domain.model.study.CoverDraft
import com.notifast.app.domain.model.study.DocxPreviewBlock
import com.notifast.app.domain.model.study.StudyDocSource
import com.notifast.app.domain.model.study.StudyDocType
import com.notifast.app.domain.model.study.StudyDocument
import com.notifast.app.domain.repository.StudyDocumentRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocalStudyDocumentRepositoryImpl(
    private val context: Context,
    private val studyDocDao: StudyDocumentDao,
    private val coverDraftDao: CoverDraftDao,
    private val docxParser: OpenXmlDocxParser,
    private val docxGenerator: OpenXmlDocxGenerator,
    docsDirOverride: File?
) : StudyDocumentRepository {

    @Inject
    constructor(
        @ApplicationContext context: Context,
        studyDocDao: StudyDocumentDao,
        coverDraftDao: CoverDraftDao,
        docxParser: OpenXmlDocxParser,
        docxGenerator: OpenXmlDocxGenerator
    ) : this(context, studyDocDao, coverDraftDao, docxParser, docxGenerator, null)

    private val studyDocsDir: File = docsDirOverride ?: File(context.filesDir, "study_docs").apply {
        if (!exists()) mkdirs()
    }

    override fun observeDocuments(userId: String): Flow<List<StudyDocument>> {
        return studyDocDao.observeDocuments(userId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun observeDocumentsBySubject(userId: String, subjectName: String): Flow<List<StudyDocument>> {
        return studyDocDao.observeDocumentsBySubject(userId, subjectName).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getDocumentById(userId: String, documentId: String): StudyDocument? {
        return studyDocDao.getDocumentById(userId, documentId)?.toDomain()
    }

    override suspend fun importDocument(
        userId: String,
        uri: Uri,
        subjectId: String,
        subjectName: String
    ): Result<StudyDocument> = withContext(Dispatchers.IO) {
        try {
            val originalName = queryFileName(uri) ?: "documento_${System.currentTimeMillis()}"
            val extension = originalName.substringAfterLast('.', "").lowercase()
            val docType = if (extension == "docx") StudyDocType.DOCX else StudyDocType.PDF
            val actualExt = if (docType == StudyDocType.DOCX) "docx" else "pdf"

            val docId = UUID.randomUUID().toString()
            val fileName = "$docId.$actualExt"
            val targetFile = File(studyDocsDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            } ?: return@withContext Result.failure(IllegalStateException("No se pudo leer el archivo seleccionado"))

            val title = originalName.substringBeforeLast('.')
            val sizeBytes = targetFile.length()
            val pageCount = if (docType == StudyDocType.DOCX) {
                FileInputStream(targetFile).use { fis ->
                    docxParser.parse(fis).getOrNull()?.size ?: 1
                }
            } else {
                1
            }

            val entity = StudyDocumentEntity(
                id = docId,
                userId = userId,
                subjectId = subjectId,
                subjectName = subjectName,
                title = title,
                fileName = fileName,
                docType = docType.name,
                sizeBytes = sizeBytes,
                pageCount = pageCount,
                sourceType = StudyDocSource.IMPORTED.name,
                googleDocId = null,
                googleWebViewLink = null,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            studyDocDao.upsertDocument(entity)
            Result.success(entity.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun duplicateDocument(
        userId: String,
        documentId: String
    ): Result<StudyDocument> = withContext(Dispatchers.IO) {
        try {
            val original = studyDocDao.getDocumentById(userId, documentId)
                ?: return@withContext Result.failure(NoSuchElementException("Documento no encontrado"))

            val originalFile = File(studyDocsDir, original.fileName)
            val newId = UUID.randomUUID().toString()
            val ext = original.fileName.substringAfterLast('.', "pdf")
            val newFileName = "$newId.$ext"
            val newFile = File(studyDocsDir, newFileName)

            if (originalFile.exists()) {
                originalFile.copyTo(newFile, overwrite = true)
            } else {
                newFile.writeBytes(ByteArray(0))
            }

            val duplicatedEntity = original.copy(
                id = newId,
                title = "${original.title} (Copia)",
                fileName = newFileName,
                sourceType = StudyDocSource.DUPLICATED.name,
                googleDocId = null,
                googleWebViewLink = null,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            studyDocDao.upsertDocument(duplicatedEntity)
            Result.success(duplicatedEntity.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun assignSubject(
        userId: String,
        documentId: String,
        subjectId: String,
        subjectName: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val doc = studyDocDao.getDocumentById(userId, documentId)
                ?: return@withContext Result.failure(NoSuchElementException("Documento no encontrado"))

            val updated = doc.copy(
                subjectId = subjectId,
                subjectName = subjectName,
                updatedAt = System.currentTimeMillis()
            )
            studyDocDao.upsertDocument(updated)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateGoogleDocLink(
        userId: String,
        documentId: String,
        googleDocId: String,
        webViewLink: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val doc = studyDocDao.getDocumentById(userId, documentId)
                ?: return@withContext Result.failure(NoSuchElementException("Documento no encontrado"))

            val updated = doc.copy(
                googleDocId = googleDocId,
                googleWebViewLink = webViewLink,
                updatedAt = System.currentTimeMillis()
            )
            studyDocDao.upsertDocument(updated)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteDocument(
        userId: String,
        documentId: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val doc = studyDocDao.getDocumentById(userId, documentId)
            if (doc != null) {
                val file = File(studyDocsDir, doc.fileName)
                if (file.exists()) {
                    file.delete()
                }
                studyDocDao.deleteDocument(userId, documentId)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getDocumentFile(fileName: String): File {
        return File(studyDocsDir, fileName)
    }

    override suspend fun parseDocxPreview(fileName: String): Result<List<DocxPreviewBlock>> = withContext(Dispatchers.IO) {
        try {
            val file = File(studyDocsDir, fileName)
            if (!file.exists()) {
                return@withContext Result.failure(NoSuchElementException("Archivo no encontrado en el almacenamiento"))
            }
            FileInputStream(file).use { fis ->
                docxParser.parse(fis)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun observeCoverDrafts(userId: String): Flow<List<CoverDraft>> {
        return coverDraftDao.observeDrafts(userId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun getLatestCoverDraft(userId: String): CoverDraft? {
        return coverDraftDao.getLatestDraft(userId)?.toDomain()
    }

    override suspend fun getCoverDraftById(userId: String, draftId: String): CoverDraft? {
        return coverDraftDao.getDraftById(userId, draftId)?.toDomain()
    }

    override suspend fun saveCoverDraft(draft: CoverDraft): Result<CoverDraft> = withContext(Dispatchers.IO) {
        try {
            val entity = CoverDraftEntity.fromDomain(draft.copy(updatedAt = System.currentTimeMillis()))
            coverDraftDao.upsertDraft(entity)
            Result.success(entity.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteCoverDraft(userId: String, draftId: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            coverDraftDao.deleteDraft(userId, draftId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun generateLocalDocxFromDraft(draft: CoverDraft): Result<StudyDocument> = withContext(Dispatchers.IO) {
        try {
            val docId = UUID.randomUUID().toString()
            val fileName = "$docId.docx"
            val targetFile = File(studyDocsDir, fileName)

            FileOutputStream(targetFile).use { fos ->
                docxGenerator.generateDocx(draft, fos).getOrThrow()
            }

            val docEntity = StudyDocumentEntity(
                id = docId,
                userId = draft.userId,
                subjectId = draft.subjectId,
                subjectName = draft.subjectName,
                title = draft.workTitle.ifBlank { "Portada ${draft.templateType.name}" },
                fileName = fileName,
                docType = StudyDocType.DOCX.name,
                sizeBytes = targetFile.length(),
                pageCount = 1,
                sourceType = StudyDocSource.GENERATED_TEMPLATE.name,
                googleDocId = draft.googleDocId,
                googleWebViewLink = draft.googleWebViewLink,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            studyDocDao.upsertDocument(docEntity)

            val updatedDraft = draft.copy(
                generatedLocalDocId = docId,
                updatedAt = System.currentTimeMillis()
            )
            coverDraftDao.upsertDraft(CoverDraftEntity.fromDomain(updatedDraft))

            Result.success(docEntity.toDomain())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun queryFileName(uri: Uri): String? {
        if (uri.scheme == "content") {
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex != -1) {
                            return cursor.getString(nameIndex)
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }
        return uri.lastPathSegment?.substringAfterLast('/')
    }
}
