package com.notifast.app.domain.usecase

import android.util.Log
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case to delete a subject and all associated evaluations, schedules and notes in cascade.
 */
class DeleteSubjectCascadeUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
    private val evaluationRepository: EvaluationRepository,
    private val scheduleRepository: ScheduleRepository
) {
    suspend operator fun invoke(userId: String, subjectName: String): Result<Unit> {
        return try {
            // 1. Delete associated evaluations
            val evaluations = evaluationRepository.getEvaluations(userId).first()
            val subjectEvals = evaluations.filter { it.subjectName.equals(subjectName, ignoreCase = true) }
            for (eval in subjectEvals) {
                evaluationRepository.deleteEvaluation(userId, eval)
            }

            // 2. Delete associated schedules
            val schedules = scheduleRepository.getSchedules(userId).first()
            val subjectSchedules = schedules.filter { it.subjectName.equals(subjectName, ignoreCase = true) }
            for (schedule in subjectSchedules) {
                scheduleRepository.deleteSchedule(userId, schedule)
            }

            // 3. Delete subject and its subcollection notes via NoteRepository
            noteRepository.deleteSubject(userId, subjectName)
        } catch (e: Exception) {
            Log.e("DeleteSubjectCascade", "Error deleting subject $subjectName in cascade for user $userId", e)
            Result.failure(e)
        }
    }
}
