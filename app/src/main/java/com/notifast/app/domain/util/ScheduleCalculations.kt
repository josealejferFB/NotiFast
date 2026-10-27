package com.notifast.app.domain.util

import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Schedule
import com.notifast.app.data.model.SubjectData
import com.notifast.app.presentation.feature.schedules.LiveClassStatus
import com.notifast.app.presentation.feature.schedules.ScheduleTimelineItem
import com.notifast.app.presentation.feature.schedules.WeeklyDayMetric
import com.notifast.app.presentation.feature.schedules.WeeklyScheduleSummary
import com.notifast.app.presentation.feature.schedules.WeeklySubjectMetric
import java.util.Calendar
import java.util.Locale

data class NextClassTiming(
    val dayName: String,
    val timeRemaining: String,
    val isOngoing: Boolean,
    val totalMinutesRemaining: Int
)

object ScheduleCalculations {

    fun timeToMinutes(timeStr: String): Int = TimeParser.toMinutes(timeStr) ?: 0

    fun calculateLiveClassStatus(todaySchedules: List<Schedule>, currentTimeStr: String): LiveClassStatus {
        if (todaySchedules.isEmpty()) return LiveClassStatus.NoClassesToday
        val nowMin = timeToMinutes(currentTimeStr)
        val sorted = todaySchedules.sortedBy { timeToMinutes(it.startTime) }

        val active = sorted.firstOrNull {
            val start = timeToMinutes(it.startTime)
            val end = timeToMinutes(it.endTime)
            nowMin in start until end
        }
        if (active != null) {
            val start = timeToMinutes(active.startTime)
            val end = timeToMinutes(active.endTime)
            val remaining = (end - nowMin).coerceAtLeast(0)
            val total = (end - start).coerceAtLeast(1)
            val progress = ((nowMin - start).toFloat() / total).coerceIn(0f, 1f)
            return LiveClassStatus.Active(active, remainingMinutes = remaining, progressFraction = progress)
        }

        val upcoming = sorted.firstOrNull { timeToMinutes(it.startTime) > nowMin }
        if (upcoming != null) {
            val startsIn = (timeToMinutes(upcoming.startTime) - nowMin).coerceAtLeast(0)
            return LiveClassStatus.Upcoming(upcoming, startsInMinutes = startsIn)
        }

        return LiveClassStatus.DayCompleted(totalClassesToday = todaySchedules.size)
    }

    fun calculateTimelineItems(
        daySchedules: List<Schedule>,
        dayOfWeek: Int,
        currentDay: Int,
        currentTimeStr: String,
        conflicts: List<Schedule>,
        subjectMap: Map<String, SubjectData> = emptyMap()
    ): List<ScheduleTimelineItem> {
        if (daySchedules.isEmpty()) return emptyList()
        val sorted = daySchedules.sortedBy { timeToMinutes(it.startTime) }
        val nowMin = timeToMinutes(currentTimeStr)
        val isToday = dayOfWeek == currentDay
        val conflictIds = conflicts.map { it.id }.toSet()

        val items = mutableListOf<ScheduleTimelineItem>()
        for (i in sorted.indices) {
            val current = sorted[i]
            val startMin = timeToMinutes(current.startTime)
            val endMin = timeToMinutes(current.endTime)
            val isCurrent = isToday && (nowMin in startMin until endMin)
            val subjectData = subjectMap[current.subjectName.lowercase().trim()]
                ?: subjectMap.values.find { it.id.isNotBlank() && it.id == current.subjectId }

            items.add(
                ScheduleTimelineItem.ClassItem(
                    schedule = current,
                    isCurrent = isCurrent,
                    isConflicted = conflictIds.contains(current.id),
                    subjectIcon = subjectData?.icon ?: "📚",
                    subjectColorHex = subjectData?.colorHex.orEmpty(),
                    subjectSection = subjectData?.section.orEmpty()
                )
            )

            if (i < sorted.size - 1) {
                val next = sorted[i + 1]
                val nextStartMin = timeToMinutes(next.startTime)
                val gap = nextStartMin - endMin
                if (gap >= 45) {
                    items.add(
                        ScheduleTimelineItem.GapItem(
                            startTime = current.endTime,
                            endTime = next.startTime,
                            durationMinutes = gap
                        )
                    )
                }
            }
        }
        return items
    }

    fun calculateConflicts(daySchedules: List<Schedule>): List<Schedule> {
        if (daySchedules.size < 2) return emptyList()
        val conflicted = mutableSetOf<Schedule>()
        for (i in 0 until daySchedules.size - 1) {
            val a = daySchedules[i]
            for (j in i + 1 until daySchedules.size) {
                val b = daySchedules[j]
                if (TimeParser.overlaps(a.startTime, a.endTime, b.startTime, b.endTime)) {
                    conflicted.add(a)
                    conflicted.add(b)
                }
            }
        }
        return conflicted.toList()
    }

    fun calculateWeeklySummary(schedules: List<Schedule>): WeeklyScheduleSummary {
        if (schedules.isEmpty()) return WeeklyScheduleSummary()
        var totalMinutes = 0
        schedules.forEach {
            val dur = (timeToMinutes(it.endTime) - timeToMinutes(it.startTime)).coerceAtLeast(0)
            totalMinutes += dur
        }

        val dayMetrics = (0..6).map { dayIdx ->
            val dayList = schedules.filter { it.dayOfWeek == dayIdx }
            val dayMins = dayList.sumOf { (timeToMinutes(it.endTime) - timeToMinutes(it.startTime)).coerceAtLeast(0) }
            WeeklyDayMetric(
                dayOfWeek = dayIdx,
                dayName = Schedule.DAY_NAMES.getOrElse(dayIdx) { "Día $dayIdx" },
                classCount = dayList.size,
                totalMinutes = dayMins
            )
        }

        val busiestDay = dayMetrics.maxByOrNull { it.classCount } ?: dayMetrics.first()

        val subjectMetrics = schedules.groupBy { it.subjectName }.map { (name, list) ->
            val subMins = list.sumOf { (timeToMinutes(it.endTime) - timeToMinutes(it.startTime)).coerceAtLeast(0) }
            WeeklySubjectMetric(
                subjectName = name,
                color = list.firstOrNull()?.color ?: 0L,
                classCount = list.size,
                totalMinutes = subMins
            )
        }.sortedByDescending { it.totalMinutes }

        return WeeklyScheduleSummary(
            totalWeeklyMinutes = totalMinutes,
            totalClasses = schedules.size,
            busiestDayName = busiestDay.dayName,
            busiestDayCount = busiestDay.classCount,
            dayMetrics = dayMetrics,
            subjectMetrics = subjectMetrics
        )
    }

    fun calculateNextClassTiming(
        schedule: Schedule,
        nowCal: Calendar = Calendar.getInstance()
    ): NextClassTiming {
        val todayDayOfWeek = nowCal.get(Calendar.DAY_OF_WEEK) - 1
        val nowMinutes = nowCal.get(Calendar.HOUR_OF_DAY) * 60 + nowCal.get(Calendar.MINUTE)
        val startMin = timeToMinutes(schedule.startTime)
        val endMin = timeToMinutes(schedule.endTime)

        val rawDaysAhead = (schedule.dayOfWeek - todayDayOfWeek + 7) % 7
        val isToday = rawDaysAhead == 0

        return if (isToday) {
            if (nowMinutes in startMin until endMin) {
                val remaining = (endMin - nowMinutes).coerceAtLeast(0)
                NextClassTiming(
                    dayName = "Hoy, ${Schedule.DAY_NAMES.getOrElse(schedule.dayOfWeek) { "Hoy" }}",
                    timeRemaining = formatOngoingRemaining(remaining),
                    isOngoing = true,
                    totalMinutesRemaining = remaining
                )
            } else if (nowMinutes < startMin) {
                val startsIn = (startMin - nowMinutes).coerceAtLeast(0)
                NextClassTiming(
                    dayName = "Hoy, ${Schedule.DAY_NAMES.getOrElse(schedule.dayOfWeek) { "Hoy" }}",
                    timeRemaining = formatStartsIn(startsIn),
                    isOngoing = false,
                    totalMinutesRemaining = startsIn
                )
            } else {
                val daysOffset = 7
                val totalStartsIn = daysOffset * 24 * 60 + startMin - nowMinutes
                val targetCal = (nowCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, daysOffset) }
                NextClassTiming(
                    dayName = formatFutureDayName(targetCal, schedule.dayOfWeek, daysOffset),
                    timeRemaining = formatStartsIn(totalStartsIn),
                    isOngoing = false,
                    totalMinutesRemaining = totalStartsIn
                )
            }
        } else {
            val totalStartsIn = rawDaysAhead * 24 * 60 + startMin - nowMinutes
            val targetCal = (nowCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, rawDaysAhead) }
            NextClassTiming(
                dayName = formatFutureDayName(targetCal, schedule.dayOfWeek, rawDaysAhead),
                timeRemaining = formatStartsIn(totalStartsIn),
                isOngoing = false,
                totalMinutesRemaining = totalStartsIn
            )
        }
    }

    private fun formatFutureDayName(targetCal: Calendar, dayOfWeek: Int, daysOffset: Int): String {
        val rawDayName = Schedule.DAY_NAMES.getOrElse(dayOfWeek) { "" }
        if (daysOffset == 1) {
            return "Mañana, $rawDayName"
        }
        val dayNum = targetCal.get(Calendar.DAY_OF_MONTH)
        val monthShort = targetCal.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.forLanguageTag("es"))
            ?.replace(".", "")
            ?.lowercase(Locale.forLanguageTag("es"))
            .orEmpty()
        return if (monthShort.isNotBlank()) {
            "$rawDayName, $dayNum $monthShort"
        } else {
            rawDayName
        }
    }

    fun formatStartsIn(startsInMinutes: Int): String {
        if (startsInMinutes <= 0) return "En breve"
        if (startsInMinutes < 60) return "En $startsInMinutes min"
        if (startsInMinutes < 1440) {
            val hours = startsInMinutes / 60
            val mins = startsInMinutes % 60
            return if (mins == 0) "En $hours h" else "En $hours h $mins min"
        }
        val days = startsInMinutes / 1440
        val hours = (startsInMinutes % 1440) / 60
        return if (days == 1) {
            if (hours == 0) "En 1 día" else "En 1 día y $hours h"
        } else {
            if (hours == 0) "En $days días" else "En $days días y $hours h"
        }
    }

    fun formatOngoingRemaining(remainingMinutes: Int): String {
        if (remainingMinutes <= 0) return "Termina ahora"
        if (remainingMinutes < 60) return "Termina en $remainingMinutes min"
        val hours = remainingMinutes / 60
        val mins = remainingMinutes % 60
        return if (mins == 0) "Termina en $hours h" else "Termina en $hours h $mins min"
    }

    fun getStartOfWeek(referenceDate: Calendar, firstDayOfWeek: String = "SUNDAY"): Calendar {
        val cal = (referenceDate.clone() as Calendar).apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val dow = (cal.get(Calendar.DAY_OF_WEEK) - 1).let { if (it == 0) 0 else it }
        val daysFromStart = if (firstDayOfWeek.equals("SUNDAY", ignoreCase = true)) {
            dow
        } else {
            (dow - 1 + 7) % 7
        }
        cal.add(Calendar.DAY_OF_YEAR, -daysFromStart)
        return cal
    }

    fun calculateEvaluationCountsForWeek(
        evaluations: List<Evaluation>,
        referenceDate: Calendar,
        firstDayOfWeek: String = "SUNDAY"
    ): Map<Int, Int> {
        if (evaluations.isEmpty()) return emptyMap()
        val startCal = getStartOfWeek(referenceDate, firstDayOfWeek)
        val startTime = startCal.timeInMillis
        val endCal = (startCal.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 7) }
        val endTime = endCal.timeInMillis

        val evalCal = Calendar.getInstance()
        val counts = mutableMapOf<Int, Int>()

        for (evaluation in evaluations) {
            val date = evaluation.date ?: continue
            val time = date.time
            if (time in startTime until endTime) {
                evalCal.time = date
                val dayOfWeek = (evalCal.get(Calendar.DAY_OF_WEEK) - 1).let { if (it == 0) 0 else it }
                counts[dayOfWeek] = (counts[dayOfWeek] ?: 0) + 1
            }
        }
        return counts
    }
}

