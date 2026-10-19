package com.notifast.app.domain.usecase

import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Schedule
import com.notifast.app.data.model.package_data.SubjectPackageCodec
import com.notifast.app.domain.model.GradePolicy
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.domain.model.package_data.ConflictStrategy
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.repository.ScheduleRepository
import com.notifast.app.domain.util.TimeParser
import kotlinx.coroutines.flow.first
import java.util.UUID
import javax.inject.Inject

class ImportSubjectPackageUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
    private val evaluationRepository: EvaluationRepository,
    private val scheduleRepository: ScheduleRepository,
    private val resolveSubjectNameUseCase: ResolveSubjectNameUseCase = ResolveSubjectNameUseCase()
) {

    suspend operator fun invoke(
        userId: String,
        compressedPayload: String,
        conflictStrategy: ConflictStrategy = ConflictStrategy.RENAME
    ): Result<String> {
        return try {
            val pkg = SubjectPackageCodec.decodeFromCompressedString(compressedPayload)
            validatePackage(pkg).getOrThrow()
            val rawName = pkg.subject.name.trim()

            val existingSubjects = noteRepository.getSubjects(userId).first()
            val nameExists = existingSubjects.any { it.name.equals(rawName, ignoreCase = true) }

            val targetName = when {
                !nameExists -> rawName
                conflictStrategy == ConflictStrategy.OVERWRITE -> {
                    noteRepository.deleteSubject(userId, rawName).getOrElse {
                        return Result.failure(it)
                    }
                    rawName
                }
                conflictStrategy == ConflictStrategy.SKIP_IF_EXISTS -> {
                    return Result.failure(IllegalStateException("La asignatura \"$rawName\" ya existe en tu cuaderno"))
                }
                else -> {
                    resolveSubjectNameUseCase.suggest(rawName, existingSubjects.map { it.name })
                }
            }

            val subjectId = noteRepository.addSubject(
                userId = userId,
                subjectName = targetName,
                icon = pkg.subject.icon.ifBlank { "📚" },
                colorHex = pkg.subject.colorHex,
                cycle = pkg.subject.cycle,
                section = pkg.subject.section.orEmpty()
            ).getOrElse { return Result.failure(it) }

            pkg.schedules.forEach { s ->
                val newSchedule = s.copy(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    subjectId = subjectId,
                    subjectName = targetName
                )
                scheduleRepository.addSchedule(userId, newSchedule).getOrElse {
                    return Result.failure(it)
                }
            }

            pkg.evaluations.forEach { e ->
                val newEval = e.copy(
                    id = UUID.randomUUID().toString(),
                    userId = userId,
                    subjectId = subjectId,
                    subjectName = targetName,
                    imageUrl = null
                )
                evaluationRepository.addEvaluation(userId, newEval, null).getOrElse {
                    return Result.failure(it)
                }
            }

            pkg.notes.forEach { n ->
                if (n.blocks.isNotEmpty()) {
                    noteRepository.addNoteWithBlocks(
                        userId = userId,
                        subject = targetName,
                        title = n.title,
                        blocks = n.blocks,
                        tags = n.tags
                    ).getOrElse { return Result.failure(it) }
                } else {
                    noteRepository.addNote(
                        userId = userId,
                        subject = targetName,
                        title = n.title,
                        content = n.content,
                        imageUri = null,
                        tags = n.tags
                    ).getOrElse { return Result.failure(it) }
                }
            }

            Result.success(targetName)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

internal fun validatePackage(pkg: com.notifast.app.data.model.package_data.SubjectPackage): Result<Unit> {
    val rawName = pkg.subject.name.trim()
    if (rawName.isBlank()) {
        return Result.failure(IllegalArgumentException("El paquete no contiene un nombre de asignatura válido"))
    }

    pkg.evaluations.forEach { e ->
        if (e.weight != null && (e.weight < 0.0 || e.weight > 100.0)) {
            return Result.failure(IllegalArgumentException("Peso inválido en evaluación '${e.title}': ${e.weight}"))
        }
        if (e.grade != null) {
            val src = com.notifast.app.domain.model.GradeScale.fromId(e.gradeScaleId)
            if (!com.notifast.app.domain.model.GradePolicy.validateGrade(e.grade, src)) {
                return Result.failure(IllegalArgumentException("Nota inválida en evaluación '${e.title}'"))
            }
        }
    }
    pkg.schedules.forEach { s ->
        if (s.dayOfWeek !in 0..6) {
            return Result.failure(IllegalArgumentException("Día inválido en horario de '${s.subjectName}'"))
        }
        if (!com.notifast.app.domain.util.TimeParser.isValidRange(s.startTime, s.endTime)) {
            return Result.failure(IllegalArgumentException("Horario inválido: ${s.startTime} - ${s.endTime}"))
        }
    }
    return Result.success(Unit)
}
