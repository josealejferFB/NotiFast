package com.notifast.app.domain.usecase

import com.notifast.app.domain.util.NameValidation
import com.notifast.app.domain.util.SubjectNameRules
import java.util.Locale
import javax.inject.Inject

class ResolveSubjectNameUseCase @Inject constructor() {

    fun suggest(baseName: String, existing: Collection<String>): String {
        val raw = baseName.trim()
        val exists = existing.any { it.trim().lowercase(Locale.ROOT) == raw.lowercase(Locale.ROOT) }
        if (!exists) {
            return raw
        }

        var counter = 1
        while (true) {
            val suffix = " ($counter)"
            val allowedBaseLength = (SubjectNameRules.MAX_LENGTH - suffix.length).coerceAtLeast(1)
            val candidate = "${raw.take(allowedBaseLength).trimEnd()}$suffix"
            val candidateExists = existing.any { it.trim().lowercase(Locale.ROOT) == candidate.lowercase(Locale.ROOT) }
            if (!candidateExists) {
                return candidate
            }
            counter++
        }
    }

    fun validate(candidate: String, existing: Collection<String>): NameValidation {
        val trimmed = candidate.trim()
        if (trimmed.isEmpty()) {
            return NameValidation.Blank
        }
        if (trimmed.length > SubjectNameRules.MAX_LENGTH) {
            return NameValidation.TooLong
        }
        val isDuplicate = existing.any { it.trim().lowercase(Locale.ROOT) == trimmed.lowercase(Locale.ROOT) }
        if (isDuplicate) {
            return NameValidation.Duplicate
        }
        return NameValidation.Valid
    }
}
