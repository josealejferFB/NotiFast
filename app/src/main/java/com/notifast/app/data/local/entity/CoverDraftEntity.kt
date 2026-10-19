package com.notifast.app.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.notifast.app.domain.model.study.CoverDraft
import com.notifast.app.domain.model.study.CoverTemplateType

@Entity(
    tableName = "cover_drafts",
    indices = [
        Index(value = ["userId"]),
        Index(value = ["subjectId"])
    ]
)
data class CoverDraftEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val templateId: String, // "APA_7", "IEEE", "INSTITUTIONAL"
    val subjectId: String,
    val subjectName: String,
    val workTitle: String,
    val subtitle: String,
    val authorsJson: String, // List<String> JSON
    val teacherName: String,
    val institutionName: String,
    val facultyOrDepartment: String,
    val city: String,
    val formattedDate: String,
    val abstractText: String,
    val keywordsText: String,
    val generatedLocalDocId: String? = null,
    val googleDocId: String? = null,
    val googleWebViewLink: String? = null,
    val createdAt: Long,
    val updatedAt: Long
) {
    fun toDomain(): CoverDraft {
        val authorsList = try {
            val type = object : TypeToken<List<String>>() {}.type
            Gson().fromJson<List<String>>(authorsJson, type) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }

        return CoverDraft(
            id = id,
            userId = userId,
            templateType = CoverTemplateType.fromId(templateId),
            subjectId = subjectId,
            subjectName = subjectName,
            workTitle = workTitle,
            subtitle = subtitle,
            authors = authorsList,
            teacherName = teacherName,
            institutionName = institutionName,
            facultyOrDepartment = facultyOrDepartment,
            city = city,
            formattedDate = formattedDate,
            abstractText = abstractText,
            keywordsText = keywordsText,
            generatedLocalDocId = generatedLocalDocId,
            googleDocId = googleDocId,
            googleWebViewLink = googleWebViewLink,
            createdAt = createdAt,
            updatedAt = updatedAt
        )
    }

    companion object {
        fun fromDomain(draft: CoverDraft): CoverDraftEntity {
            return CoverDraftEntity(
                id = draft.id,
                userId = draft.userId,
                templateId = draft.templateType.id,
                subjectId = draft.subjectId,
                subjectName = draft.subjectName,
                workTitle = draft.workTitle,
                subtitle = draft.subtitle,
                authorsJson = Gson().toJson(draft.authors),
                teacherName = draft.teacherName,
                institutionName = draft.institutionName,
                facultyOrDepartment = draft.facultyOrDepartment,
                city = draft.city,
                formattedDate = draft.formattedDate,
                abstractText = draft.abstractText,
                keywordsText = draft.keywordsText,
                generatedLocalDocId = draft.generatedLocalDocId,
                googleDocId = draft.googleDocId,
                googleWebViewLink = draft.googleWebViewLink,
                createdAt = draft.createdAt,
                updatedAt = draft.updatedAt
            )
        }
    }
}
