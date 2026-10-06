package com.notifast.app.domain.util

import com.notifast.app.data.model.Schedule
import com.notifast.app.presentation.feature.schedules.LiveClassStatus
import com.notifast.app.presentation.feature.schedules.ScheduleTimelineItem
import com.notifast.app.presentation.feature.schedules.WeeklyDayMetric
import com.notifast.app.presentation.feature.schedules.WeeklyScheduleSummary
import com.notifast.app.presentation.feature.schedules.WeeklySubjectMetric

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
        conflicts: List<Schedule>
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

            items.add(
                ScheduleTimelineItem.ClassItem(
                    schedule = current,
                    isCurrent = isCurrent,
                    isConflicted = conflictIds.contains(current.id)
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
}
