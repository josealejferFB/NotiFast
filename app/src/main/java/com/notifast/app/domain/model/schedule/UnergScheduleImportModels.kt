package com.notifast.app.domain.model.schedule

import com.notifast.app.data.model.Schedule

/**
 * Raw row extracted from UNERG PDF class schedule table.
 */
data class UnergRawScheduleRow(
    val subjectName: String,
    val section: String,
    val hoursCount: Int,
    val dayOfWeek: Int, // 1 (Mon) to 7 (Sun) matching java.time / NotiFast convention
    val startTime: String, // "HH:mm" (24h)
    val endTime: String,   // "HH:mm" (24h)
    val classroom: String,
    val isPresencial: Boolean
)

/**
 * Subject parsed and grouped for the interactive preview sheet.
 */
data class UnergImportPreviewSubject(
    val subjectName: String,
    val section: String,
    val hoursPerWeek: Int,
    val classroomSummary: String,
    val schedules: List<Schedule>,
    val isExistingSubject: Boolean,
    val existingSubjectId: String? = null,
    val selected: Boolean = true,
    val colorHex: String = "#4338CA",
    val icon: String = "📚"
)

/**
 * Complete parsed preview data presented to the user before confirming import.
 */
data class UnergImportPreviewData(
    val studentCi: String = "",
    val career: String = "",
    val academicPeriod: String = "",
    val subjects: List<UnergImportPreviewSubject> = emptyList(),
    val totalPresencialHours: Int = 0,
    val ignoredNonPresencialCount: Int = 0
)

/**
 * Strategy to handle schedules when an existing subject already has class schedules in DB.
 */
enum class UnergImportConflictStrategy {
    MERGE_SMART,            // Conserve existing non-overlapping schedules and add new ones
    REPLACE_ALL_SCHEDULES   // Delete existing schedules for imported subjects and insert new ones
}

/**
 * Summary result after executing the import process.
 */
data class UnergImportResult(
    val createdSubjectsCount: Int = 0,
    val reusedSubjectsCount: Int = 0,
    val createdSchedulesCount: Int = 0,
    val replacedSchedulesCount: Int = 0
)
