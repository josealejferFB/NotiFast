package com.notifast.app.domain.usecase

import com.notifast.app.data.model.package_data.SubjectPackage
import com.notifast.app.data.model.package_data.SubjectPackageCodec
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class ExportSubjectPackageUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
    private val evaluationRepository: EvaluationRepository,
    private val scheduleRepository: ScheduleRepository
) {

    suspend operator fun invoke(
        userId: String,
        subjectName: String,
        includeNotes: Boolean = false
    ): Result<String> {
        return try {
            val subjects = noteRepository.getSubjects(userId).first()
            val targetSubject = subjects.find { it.name.equals(subjectName.trim(), ignoreCase = true) }
                ?: return Result.failure(IllegalArgumentException("No se encontró la asignatura \"$subjectName\""))

            val schedules = scheduleRepository.getSchedules(userId).first()
                .filter { it.subjectName.equals(targetSubject.name, ignoreCase = true) }
                .map { it.copy(userId = "") } // Sanitize user ID

            val evaluations = evaluationRepository.getEvaluations(userId).first()
                .filter { it.subjectName.equals(targetSubject.name, ignoreCase = true) }
                .map { it.copy(userId = "", imageUrl = null) } // Sanitize private image URLs & user ID

            val notes = if (includeNotes) {
                noteRepository.getNotesBySubject(userId, targetSubject.name).first()
                    .map { it.copy(userId = "", imageUrl = null) }
            } else {
                emptyList()
            }

            val pkg = SubjectPackage(
                schemaVersion = SubjectPackageCodec.CURRENT_SCHEMA_VERSION,
                exportTimestamp = System.currentTimeMillis(),
                subject = targetSubject,
                schedules = schedules,
                evaluations = evaluations,
                notes = notes
            )

            val compressedString = SubjectPackageCodec.encodeToCompressedString(pkg)
            Result.success(compressedString)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
