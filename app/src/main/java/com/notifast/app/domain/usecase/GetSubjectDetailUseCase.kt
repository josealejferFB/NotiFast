package com.notifast.app.domain.usecase

import com.notifast.app.data.model.SubjectDetailData
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Use case to observe aggregated data for a specific subject (subject info, notes, evaluations, schedules, average).
 */
class GetSubjectDetailUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
    private val evaluationRepository: EvaluationRepository,
    private val scheduleRepository: ScheduleRepository,
    private val calculateWeightedAverageUseCase: CalculateWeightedAverageUseCase
) {
    operator fun invoke(userId: String, subjectName: String): Flow<SubjectDetailData?> {
        return combine(
            noteRepository.getSubjects(userId),
            noteRepository.getNotes(userId),
            evaluationRepository.getEvaluations(userId),
            scheduleRepository.getSchedules(userId)
        ) { subjects, allNotes, allEvaluations, allSchedules ->
            val subject = subjects.find { it.name.equals(subjectName, ignoreCase = true) }
                ?: return@combine null

            val subjectNotes = allNotes.filter { it.subject.equals(subjectName, ignoreCase = true) }
            val subjectEvaluations = allEvaluations.filter { it.subjectName.equals(subjectName, ignoreCase = true) }
            val subjectSchedules = allSchedules.filter { it.subjectName.equals(subjectName, ignoreCase = true) }
            val averageGrade = calculateWeightedAverageUseCase(subjectEvaluations)

            SubjectDetailData(
                subject = subject,
                notes = subjectNotes,
                evaluations = subjectEvaluations,
                schedules = subjectSchedules,
                averageGrade = averageGrade
            )
        }
    }
}
