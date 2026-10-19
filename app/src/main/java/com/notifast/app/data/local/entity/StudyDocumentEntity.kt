package com.notifast.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.notifast.app.domain.model.study.StudyDocSource
import com.notifast.app.domain.model.study.StudyDocType
import com.notifast.app.domain.model.study.StudyDocument

@Entity(
    tableName = "study_documents",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["subjectId"])
    ]
)
data class StudyDocumentEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val subjectId: String,
    val subjectName: String,
    val title: String,
    val fileName: String,
    val docType: String, // "PDF" or "DOCX"
    val sizeBytes: Long,
    val pageCount: Int,
    val sourceType: String, // "IMPORTED", "DUPLICATED", "GENERATED_TEMPLATE"
    val googleDocId: String? = null,
    val googleWebViewLink: String? = null,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomain(): StudyDocument {
        return StudyDocument(
            id = id,
            userId = userId,
            subjectId = subjectId,
            subjectName = subjectName,
            title = title,
            fileName = fileName,
            docType = when (docType.uppercase()) {
                "DOCX" -> StudyDocType.DOCX
                else -> StudyDocType.PDF
            },
            sizeBytes = sizeBytes,
            pageCount = pageCount,
            sourceType = when (sourceType.uppercase()) {
                "DUPLICATED" -> StudyDocSource.DUPLICATED
                "GENERATED_TEMPLATE" -> StudyDocSource.GENERATED_TEMPLATE
                else -> StudyDocSource.IMPORTED
            },
            googleDocId = googleDocId,
            googleWebViewLink = googleWebViewLink,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(doc: StudyDocument): StudyDocumentEntity {
            return StudyDocumentEntity(
                id = doc.id,
                userId = doc.userId,
                subjectId = doc.subjectId,
                subjectName = doc.subjectName,
                title = doc.title,
                fileName = doc.fileName,
                docType = doc.docType.name,
                sizeBytes = doc.sizeBytes,
                pageCount = doc.pageCount,
                sourceType = doc.sourceType.name,
                googleDocId = doc.googleDocId,
                googleWebViewLink = doc.googleWebViewLink,
                createdAt = doc.createdAt,
                updatedAt = doc.updatedAt
            )
        }
    }
}
