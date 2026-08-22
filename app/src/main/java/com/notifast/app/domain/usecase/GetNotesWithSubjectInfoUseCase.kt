package com.notifast.app.domain.usecase

import com.notifast.app.data.model.NoteWithSubject
import com.notifast.app.domain.repository.NoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

/**
 * Use case that joins Notes with their corresponding Subject's icon and color for unified rendering.
 */
class GetNotesWithSubjectInfoUseCase @Inject constructor(
    private val noteRepository: NoteRepository
) {
    operator fun invoke(userId: String): Flow<List<NoteWithSubject>> {
        return combine(
            noteRepository.getNotes(userId),
            noteRepository.getSubjects(userId)
        ) { notes, subjects ->
            val subjectMap = subjects.associateBy { it.name.trim().lowercase() }
            notes.map { note ->
                val subjectData = subjectMap[note.subject.trim().lowercase()]
                NoteWithSubject(
                    note = note,
                    subjectIcon = subjectData?.icon ?: "📚",
                    subjectColorHex = subjectData?.colorHex ?: ""
                )
            }
        }
    }
}
