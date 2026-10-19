package com.notifast.app.domain.usecase.study

import com.notifast.app.domain.model.study.CoverDraft
import com.notifast.app.domain.model.study.CoverTemplateType
import com.notifast.app.domain.model.study.StudyDocument
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.StudyDocumentRepository
import com.notifast.app.domain.repository.TeacherRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject

class ObserveCoverDraftsUseCase @Inject constructor(
    private val repository: StudyDocumentRepository
) {
    operator fun invoke(userId: String): Flow<List<CoverDraft>> = repository.observeCoverDrafts(userId)
}

class SaveCoverDraftUseCase @Inject constructor(
    private val repository: StudyDocumentRepository
) {
    suspend operator fun invoke(draft: CoverDraft): Result<CoverDraft> = repository.saveCoverDraft(draft)
}

class DeleteCoverDraftUseCase @Inject constructor(
    private val repository: StudyDocumentRepository
) {
    suspend operator fun invoke(userId: String, draftId: String): Result<Unit> =
        repository.deleteCoverDraft(userId, draftId)
}

class GenerateLocalCoverDocxUseCase @Inject constructor(
    private val repository: StudyDocumentRepository
) {
    suspend operator fun invoke(draft: CoverDraft): Result<StudyDocument> =
        repository.generateLocalDocxFromDraft(draft)
}

class PrefillCoverDraftForSubjectUseCase @Inject constructor(
    private val authRepository: AuthRepository,
    private val teacherRepository: TeacherRepository,
    private val studyDocumentRepository: StudyDocumentRepository
) {
    suspend operator fun invoke(
        userId: String,
        templateType: CoverTemplateType,
        subjectId: String = "",
        subjectName: String = ""
    ): CoverDraft {
        // 1. Author from user profile
        val user = authRepository.getUserProfile(userId).getOrNull()
        val authorName = if (user != null) {
            "${user.firstName} ${user.lastName}".trim()
        } else ""

        // 2. Teacher assigned to subject
        val teacherName = try {
            val teachers = teacherRepository.getTeachers(userId).first()
            val matched = teachers.firstOrNull { t ->
                t.subjectNames.any { it.equals(subjectName, ignoreCase = true) }
            }
            if (matched != null) {
                "Prof. ${matched.firstName} ${matched.lastName}".trim()
            } else ""
        } catch (_: Exception) {
            ""
        }

        // 3. Institution from latest draft
        val latestDraft = studyDocumentRepository.getLatestCoverDraft(userId)
        val institutionName = latestDraft?.institutionName.orEmpty()
        val facultyOrDepartment = latestDraft?.facultyOrDepartment.orEmpty()
        val city = latestDraft?.city.orEmpty()

        // 4. Formatted date in Spanish
        val dateStr = try {
            val sdf = SimpleDateFormat("d 'de' MMMM 'de' yyyy", Locale.forLanguageTag("es"))
            sdf.format(Date())
        } catch (_: Exception) {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        }

        return CoverDraft(
            id = UUID.randomUUID().toString(),
            userId = userId,
            templateType = templateType,
            subjectId = subjectId,
            subjectName = subjectName,
            workTitle = "",
            subtitle = "",
            authors = if (authorName.isNotBlank()) listOf(authorName) else emptyList(),
            teacherName = teacherName,
            institutionName = institutionName,
            facultyOrDepartment = facultyOrDepartment,
            city = city,
            formattedDate = dateStr
        )
    }
}
