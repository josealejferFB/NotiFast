package com.notifast.app.domain.usecase

import android.net.Uri
import com.notifast.app.data.model.Schedule
import com.notifast.app.domain.model.search.SearchFilter
import com.notifast.app.domain.model.search.SearchResultItem
import com.notifast.app.domain.model.search.SearchResultType
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.repository.ScheduleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import java.text.Normalizer
import javax.inject.Inject

class SearchGlobalUseCase @Inject constructor(
    private val noteRepository: NoteRepository,
    private val evaluationRepository: EvaluationRepository,
    private val scheduleRepository: ScheduleRepository
) {

    operator fun invoke(
        userId: String,
        query: String,
        filter: SearchFilter = SearchFilter.ALL
    ): Flow<List<SearchResultItem>> {
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            return flowOf(emptyList())
        }

        val tokens = trimmed.normalize().split("\\s+".toRegex()).filter { it.isNotBlank() }

        return combine(
            noteRepository.getSubjects(userId),
            noteRepository.getNotes(userId),
            evaluationRepository.getEvaluations(userId),
            scheduleRepository.getSchedules(userId)
        ) { subjects, notes, evaluations, schedules ->
            val results = mutableListOf<SearchResultItem>()
            val subjectColorMap = subjects.associate { it.name to it.colorHex }

            // 1. Materias
            if (filter == SearchFilter.ALL || filter == SearchFilter.SUBJECTS) {
                subjects.filter { subject ->
                    tokens.all { token -> subject.name.normalize().contains(token) }
                }.forEach { subject ->
                    results.add(
                        SearchResultItem(
                            id = subject.name,
                            type = SearchResultType.SUBJECT,
                            title = subject.name,
                            subtitle = "Asignatura académica",
                            snippet = null,
                            subjectName = subject.name,
                            subjectColorHex = subject.colorHex,
                            badgeText = "Materia",
                            targetRoute = "subject_detail/${encode(subject.name)}"
                        )
                    )
                }
            }

            // 2. Apuntes
            if (filter == SearchFilter.ALL || filter == SearchFilter.NOTES) {
                notes.filter { note ->
                    val combinedText = "${note.title} ${note.content} ${note.subject} ${note.tags.joinToString(" ")}".normalize()
                    tokens.all { token -> combinedText.contains(token) }
                }.forEach { note ->
                    val snippetText = if (note.content.isNotBlank()) {
                        if (note.content.length > 80) note.content.take(80) + "..." else note.content
                    } else null

                    results.add(
                        SearchResultItem(
                            id = note.id,
                            type = SearchResultType.NOTE,
                            title = note.title.ifBlank { "Apunte sin título" },
                            subtitle = note.subject,
                            snippet = snippetText,
                            subjectName = note.subject,
                            subjectColorHex = subjectColorMap[note.subject].orEmpty(),
                            badgeText = if (note.tags.isNotEmpty()) "#${note.tags.first()}" else "Apunte",
                            targetRoute = "edit_note/${encode(note.id)}"
                        )
                    )
                }
            }

            // 3. Evaluaciones
            if (filter == SearchFilter.ALL || filter == SearchFilter.EVALUATIONS) {
                evaluations.filter { eval ->
                    val combinedText = "${eval.title} ${eval.description} ${eval.subjectName} ${eval.type} ${eval.quickTips}".normalize()
                    tokens.all { token -> combinedText.contains(token) }
                }.forEach { eval ->
                    val weightText = eval.weight?.let { "${it.toInt()}%" }
                    val snippetText = if (eval.description.isNotBlank()) {
                        if (eval.description.length > 80) eval.description.take(80) + "..." else eval.description
                    } else null

                    results.add(
                        SearchResultItem(
                            id = eval.id,
                            type = SearchResultType.EVALUATION,
                            title = eval.title.ifBlank { "Evaluación" },
                            subtitle = eval.subjectName,
                            snippet = snippetText,
                            subjectName = eval.subjectName,
                            subjectColorHex = subjectColorMap[eval.subjectName].orEmpty(),
                            badgeText = weightText ?: eval.type,
                            targetRoute = "edit_evaluation/${encode(eval.id)}"
                        )
                    )
                }
            }

            // 4. Horarios
            if (filter == SearchFilter.ALL || filter == SearchFilter.SCHEDULES) {
                schedules.filter { schedule ->
                    val dayName = Schedule.DAY_NAMES.getOrElse(schedule.dayOfWeek) { "" }
                    val combinedText = "${schedule.subjectName} ${schedule.teacher} ${schedule.classroom} ${schedule.modality} $dayName".normalize()
                    tokens.all { token -> combinedText.contains(token) }
                }.forEach { schedule ->
                    val dayName = Schedule.DAY_NAMES.getOrElse(schedule.dayOfWeek) { "" }
                    val timeAndLocation = buildString {
                        append("$dayName ${schedule.startTime} - ${schedule.endTime}")
                        if (schedule.classroom.isNotBlank()) append(" • ${schedule.classroom}")
                    }

                    results.add(
                        SearchResultItem(
                            id = schedule.id,
                            type = SearchResultType.SCHEDULE,
                            title = "Clase de ${schedule.subjectName}",
                            subtitle = if (schedule.teacher.isNotBlank()) "$timeAndLocation • ${schedule.teacher}" else timeAndLocation,
                            snippet = null,
                            subjectName = schedule.subjectName,
                            subjectColorHex = subjectColorMap[schedule.subjectName].orEmpty(),
                            badgeText = schedule.modality.replaceFirstChar { it.titlecase() },
                            targetRoute = "agenda"
                        )
                    )
                }
            }

            results
        }
    }

    private fun encode(value: String): String = try {
        java.net.URLEncoder.encode(value, "UTF-8").replace("+", "%20")
    } catch (e: Exception) {
        value
    }

    private fun String.normalize(): String =
        Normalizer.normalize(this, Normalizer.Form.NFD)
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
            .lowercase()
            .trim()
}
