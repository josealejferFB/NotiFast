package com.notifast.app.domain.usecase.study

import android.net.Uri
import com.notifast.app.domain.model.study.DocxPreviewBlock
import com.notifast.app.domain.model.study.StudyDocument
import com.notifast.app.domain.repository.StudyDocumentRepository
import kotlinx.coroutines.flow.Flow
import java.io.File
import javax.inject.Inject

class ObserveStudyDocumentsUseCase @Inject constructor(
    private val repository: StudyDocumentRepository
) {
    operator fun invoke(userId: String): Flow<List<StudyDocument>> =
        repository.observeDocuments(userId)
}

class ObserveStudyDocumentsBySubjectUseCase @Inject constructor(
    private val repository: StudyDocumentRepository
) {
    operator fun invoke(userId: String, subjectName: String): Flow<List<StudyDocument>> =
        repository.observeDocumentsBySubject(userId, subjectName)
}

class ImportStudyDocumentUseCase @Inject constructor(
    private val repository: StudyDocumentRepository
) {
    suspend operator fun invoke(
        userId: String,
        uri: Uri,
        subjectId: String = "",
        subjectName: String = ""
    ): Result<StudyDocument> = repository.importDocument(userId, uri, subjectId, subjectName)
}

class DuplicateStudyDocumentUseCase @Inject constructor(
    private val repository: StudyDocumentRepository
) {
    suspend operator fun invoke(userId: String, documentId: String): Result<StudyDocument> =
        repository.duplicateDocument(userId, documentId)
}

class AssignDocumentSubjectUseCase @Inject constructor(
    private val repository: StudyDocumentRepository
) {
    suspend operator fun invoke(
        userId: String,
        documentId: String,
        subjectId: String,
        subjectName: String
    ): Result<Unit> = repository.assignSubject(userId, documentId, subjectId, subjectName)
}

class DeleteStudyDocumentUseCase @Inject constructor(
    private val repository: StudyDocumentRepository
) {
    suspend operator fun invoke(userId: String, documentId: String): Result<Unit> =
        repository.deleteDocument(userId, documentId)
}

class GetStudyDocumentFileUseCase @Inject constructor(
    private val repository: StudyDocumentRepository
) {
    operator fun invoke(fileName: String): File = repository.getDocumentFile(fileName)
}

class ParseDocxPreviewUseCase @Inject constructor(
    private val repository: StudyDocumentRepository
) {
    suspend operator fun invoke(fileName: String): Result<List<DocxPreviewBlock>> =
        repository.parseDocxPreview(fileName)
}
