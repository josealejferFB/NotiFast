package com.notifast.app.domain.usecase

import com.notifast.app.data.model.package_data.SubjectPackageCodec
import com.notifast.app.domain.model.package_data.ConflictStrategy
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject

class ImportSubjectPackageUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
    private val evaluationRepository: EvaluationRepository,
    private val scheduleRepository: ScheduleRepository
) {

    suspend operator fun invoke(
        userId: String,
        compressedPayload: String,
        conflictStrategy: ConflictStrategy = ConflictStrategy.RENAME
    ): Result<String> {
        return try {
            val pkg = SubjectPackageCodec.decodeFromCompressedString(compressedPayload)
            val rawName = pkg.subject.name.trim()
            if (rawName.isBlank()) {
                return Result.failure(IllegalArgumentException("El paquete no contiene un nombre de asignatura válido"))
            }

            val existingSubjects = noteRepository.getSubjects(userId).first()
            val nameExists = existingSubjects.any { it.name.equals(rawName, ignoreCase = true) }

            val targetName = when {
                !nameExists -> rawName
                conflictStrategy == ConflictStrategy.OVERWRITE -> rawName
                conflictStrategy == ConflictStrategy.SKIP_IF_EXISTS -> {
                    return Result.failure(IllegalStateException("La asignatura \"$rawName\" ya existe en tu cuaderno"))
                }
                else -> {
                    // RENAME Strategy
                    var suffix = " (Copia)"
                    var candidate = "$rawName$suffix"
                    var counter = 2
                    while (existingSubjects.any { it.name.equals(candidate, ignoreCase = true) }) {
                        candidate = "$rawName (Copia $counter)"
                        counter++
                    }
                    candidate
                }
            }

            // 1. Guardar Materia
            noteRepository.addSubject(
                userId = userId,
                subjectName = targetName,
                icon = pkg.subject.icon.ifBlank { "📚" },
                colorHex = pkg.subject.colorHex
            )

            // 2. Guardar Horarios
            pkg.schedules.forEach { s ->
                val newSchedule = s.copy(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    subjectName = targetName
                )
                scheduleRepository.addSchedule(userId, newSchedule)
            }

            // 3. Guardar Evaluaciones
            pkg.evaluations.forEach { e ->
                val newEval = e.copy(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    subjectName = targetName,
                    imageUrl = null
                )
                evaluationRepository.addEvaluation(userId, newEval, null)
            }

            // 4. Guardar Apuntes (si existen)
            pkg.notes.forEach { n ->
                noteRepository.addNote(
                    userId = userId,
                    subject = targetName,
                    title = n.title,
                    content = n.content,
                    imageUri = null,
                    tags = n.tags
                )
            }

            Result.success(targetName)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
