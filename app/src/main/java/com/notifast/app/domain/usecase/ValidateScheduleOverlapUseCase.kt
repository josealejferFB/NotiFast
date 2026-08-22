package com.notifast.app.domain.usecase

import com.notifast.app.data.model.Schedule
import javax.inject.Inject

sealed interface ScheduleOverlapResult {
    data object NoOverlap : ScheduleOverlapResult
    data class Conflict(val conflictingSchedules: List<Schedule>) : ScheduleOverlapResult
}

/**
 * Use case to detect time conflicts between class schedules on the same day.
 * Schedule interval: [startTime, endTime)
 */
class ValidateScheduleOverlapUseCase @Inject constructor() {

    operator fun invoke(
        existingSchedules: List<Schedule>,
        newSchedule: Schedule,
        excludeScheduleId: String? = null
    ): ScheduleOverlapResult {
        val newStartMin = parseTimeToMinutes(newSchedule.startTime) ?: return ScheduleOverlapResult.NoOverlap
        val newEndMin = parseTimeToMinutes(newSchedule.endTime) ?: return ScheduleOverlapResult.NoOverlap

        if (newEndMin <= newStartMin) return ScheduleOverlapResult.NoOverlap

        val conflicts = existingSchedules.filter { existing ->
            if (existing.id.isNotBlank() && (existing.id == excludeScheduleId || existing.id == newSchedule.id)) {
                return@filter false
            }
            if (existing.dayOfWeek != newSchedule.dayOfWeek) return@filter false

            val existingStart = parseTimeToMinutes(existing.startTime) ?: return@filter false
            val existingEnd = parseTimeToMinutes(existing.endTime) ?: return@filter false

            if (existingEnd <= existingStart) return@filter false

            // Interval overlap condition: max(start1, start2) < min(end1, end2)
            maxOf(newStartMin, existingStart) < minOf(newEndMin, existingEnd)
        }

        return if (conflicts.isEmpty()) {
            ScheduleOverlapResult.NoOverlap
        } else {
            ScheduleOverlapResult.Conflict(conflicts)
        }
    }

    private fun parseTimeToMinutes(timeStr: String): Int? {
        val parts = timeStr.trim().split(":")
        if (parts.size != 2) return null
        val hours = parts[0].toIntOrNull() ?: return null
        val minutes = parts[1].toIntOrNull() ?: return null
        if (hours !in 0..23 || minutes !in 0..59) return null
        return hours * 60 + minutes
    }
}
