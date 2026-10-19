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
    val tutorName: String = "",
    val fontName: String = "Times New Roman",
    val fontSizePt: Int = 12,
    val countryLine: String = "",
    val ministryLine: String = "",
    val programLine: String = "",
    val curricularUnit: String = "",
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
            tutorName = tutorName,
            fontName = fontName.ifBlank { "Times New Roman" },
            fontSizePt = if (fontSizePt > 0) fontSizePt else 12,
            institutionName = institutionName,
            facultyOrDepartment = facultyOrDepartment,
            city = city,
            formattedDate = formattedDate,
            abstractText = abstractText,
            keywordsText = keywordsText,
            countryLine = countryLine.ifBlank { "REPÚBLICA BOLIVARIANA DE VENEZUELA" },
            ministryLine = ministryLine.ifBlank { "MINISTERIO DEL PODER POPULAR PARA LA EDUCACIÓN UNIVERSITARIA" },
            programLine = programLine.ifBlank { "PROGRAMA DE INGENIERÍA EN INFORMÁTICA" },
            curricularUnit = curricularUnit.ifBlank { "UNIDAD CURRICULAR: PROYECTO DE GRADO I" },
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
                tutorName = draft.tutorName,
                fontName = draft.fontName,
                fontSizePt = draft.fontSizePt,
                institutionName = draft.institutionName,
                facultyOrDepartment = draft.facultyOrDepartment,
                city = draft.city,
                formattedDate = draft.formattedDate,
                abstractText = draft.abstractText,
                keywordsText = draft.keywordsText,
                countryLine = draft.countryLine,
                ministryLine = draft.ministryLine,
                programLine = draft.programLine,
                curricularUnit = draft.curricularUnit,
                generatedLocalDocId = draft.generatedLocalDocId,
                googleDocId = draft.googleDocId,
                googleWebViewLink = draft.googleWebViewLink,
                createdAt = draft.createdAt,
                updatedAt = draft.updatedAt
            )
        }
    }
}
