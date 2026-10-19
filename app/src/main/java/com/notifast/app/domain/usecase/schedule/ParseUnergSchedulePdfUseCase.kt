package com.notifast.app.domain.usecase.schedule

import com.notifast.app.domain.model.schedule.UnergImportPreviewData
import com.notifast.app.domain.repository.NoteRepository
import com.notifast.app.domain.util.UnergSchedulePdfParser
import kotlinx.coroutines.flow.first
import java.io.InputStream
import java.text.Normalizer
import javax.inject.Inject

/**
 * Parses a UNERG schedule PDF and enriches it by cross-referencing existing subjects
 * in the user's database.
 */
class ParseUnergSchedulePdfUseCase @Inject constructor(
    private val parser: UnergSchedulePdfParser,
    private val noteRepository: NoteRepository
) {

    suspend operator fun invoke(userId: String, inputStream: InputStream): Result<UnergImportPreviewData> {
        val parseResult = parser.parse(inputStream)
        if (parseResult.isFailure) {
            return parseResult
        }

        val previewData = parseResult.getOrThrow()
        val existingSubjects = try {
            noteRepository.getSubjects(userId).first()
        } catch (_: Exception) {
            emptyList()
        }

        val enrichedSubjects = previewData.subjects.map { previewSubject ->
            val match = existingSubjects.find { existing ->
                normalizeName(existing.name) == normalizeName(previewSubject.subjectName)
            }

            if (match != null) {
                previewSubject.copy(
                    isExistingSubject = true,
                    existingSubjectId = match.id,
                    colorHex = match.colorHex.ifBlank { previewSubject.colorHex },
                    icon = match.icon.ifBlank { previewSubject.icon }
                )
            } else {
                previewSubject
            }
        }

        return Result.success(previewData.copy(subjects = enrichedSubjects))
    }

    private fun normalizeName(text: String): String {
        val normalized = Normalizer.normalize(text, Normalizer.Form.NFD)
        return normalized.replace(Regex("\\p{M}"), "").trim().lowercase()
    }
}
