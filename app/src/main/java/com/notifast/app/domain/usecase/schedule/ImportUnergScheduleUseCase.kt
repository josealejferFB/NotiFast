package com.notifast.app.domain.usecase.schedule

import com.notifast.app.domain.model.schedule.UnergImportConflictStrategy
import com.notifast.app.domain.model.schedule.UnergImportPreviewSubject
import com.notifast.app.domain.model.schedule.UnergImportResult
import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Executes the UNERG schedule import process:
 * creates/updates subjects with their section and persists class schedules.
 */
class ImportUnergScheduleUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
    private val scheduleRepository: ScheduleRepository
) {

    suspend operator fun invoke(
        userId: String,
        previewSubjects: List<UnergImportPreviewSubject>,
        conflictStrategy: UnergImportConflictStrategy
    ): Result<UnergImportResult> {
        return try {
            val selected = previewSubjects.filter { it.selected }
            if (selected.isEmpty()) {
                return Result.success(UnergImportResult())
            }

            var createdSubjectsCount = 0
            var reusedSubjectsCount = 0
            var createdSchedulesCount = 0
            var replacedSchedulesCount = 0

            val existingSchedules = try {
                scheduleRepository.getSchedules(userId).first()
            } catch (_: Exception) {
                emptyList()
            }

            val existingSubjects = try {
                noteRepository.getSubjects(userId).first()
            } catch (_: Exception) {
                emptyList()
            }

            for (item in selected) {
                val finalSubjectId: String
                val finalSubjectName = item.subjectName

                if (item.isExistingSubject && !item.existingSubjectId.isNullOrBlank()) {
                    finalSubjectId = item.existingSubjectId
                    reusedSubjectsCount++

                    // Update section on existing subject if changed
                    val existingSub = existingSubjects.find { it.id == finalSubjectId }
                    if (existingSub != null && existingSub.section != item.section && item.section.isNotBlank()) {
                        noteRepository.updateSubject(userId, existingSub.copy(section = item.section))
                    }

                    // Handle conflict strategy
                    if (conflictStrategy == UnergImportConflictStrategy.REPLACE_ALL_SCHEDULES) {
                        val toDelete = existingSchedules.filter {
                            it.subjectId == finalSubjectId ||
                                (it.subjectId.isBlank() && it.subjectName.equals(finalSubjectName, ignoreCase = true))
                        }
                        for (s in toDelete) {
                            scheduleRepository.deleteSchedule(userId, s)
                            replacedSchedulesCount++
                        }
                    }
                } else {
                    // Create new subject
                    val addResult = noteRepository.addSubject(
                        userId = userId,
                        subjectName = finalSubjectName,
                        icon = item.icon.ifBlank { "📚" },
                        colorHex = item.colorHex,
                        cycle = "",
                        section = item.section
                    )
                    finalSubjectId = addResult.getOrThrow()
                    createdSubjectsCount++
                }

                // Insert schedules
                for (schedule in item.schedules) {
                    val toInsert = schedule.copy(
                        userId = userId,
                        subjectId = finalSubjectId,
                        subjectName = finalSubjectName
                    )
                    scheduleRepository.addSchedule(userId, toInsert).getOrThrow()
                    createdSchedulesCount++
                }
            }

            Result.success(
                UnergImportResult(
                    createdSubjectsCount = createdSubjectsCount,
                    reusedSubjectsCount = reusedSubjectsCount,
                    createdSchedulesCount = createdSchedulesCount,
                    replacedSchedulesCount = replacedSchedulesCount
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
