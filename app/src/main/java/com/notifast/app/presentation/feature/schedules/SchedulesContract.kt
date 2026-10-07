package com.notifast.app.presentation.feature.schedules

import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Schedule
import com.notifast.app.domain.model.GradeScale
import java.util.Calendar

enum class ScheduleViewMode {
    DAILY_TIMELINE,
    MONTHLY_CALENDAR,
    WEEKLY_BENTO
}

sealed interface LiveClassStatus {
    data class Active(
        val schedule: Schedule,
        val remainingMinutes: Int,
        val progressFraction: Float = 0f
    ) : LiveClassStatus

    data class Upcoming(
        val schedule: Schedule,
        val startsInMinutes: Int
    ) : LiveClassStatus

    data class DayCompleted(
        val totalClassesToday: Int
    ) : LiveClassStatus

    data object NoClassesToday : LiveClassStatus
}

sealed interface ScheduleTimelineItem {
    data class ClassItem(
        val schedule: Schedule,
        val isCurrent: Boolean = false,
        val isConflicted: Boolean = false
    ) : ScheduleTimelineItem

    data class GapItem(
        val startTime: String,
        val endTime: String,
        val durationMinutes: Int
    ) : ScheduleTimelineItem
}

data class WeeklyDayMetric(
    val dayOfWeek: Int,
    val dayName: String,
    val classCount: Int,
    val totalMinutes: Int
)

data class WeeklySubjectMetric(
    val subjectName: String,
    val color: Long,
    val classCount: Int,
    val totalMinutes: Int
)

data class WeeklyScheduleSummary(
    val totalWeeklyMinutes: Int = 0,
    val totalClasses: Int = 0,
    val busiestDayName: String = "",
    val busiestDayCount: Int = 0,
    val dayMetrics: List<WeeklyDayMetric> = emptyList(),
    val subjectMetrics: List<WeeklySubjectMetric> = emptyList()
)

data class SchedulesUiState(
    val isLoading: Boolean = false,
    val schedules: List<Schedule> = emptyList(),
    val groupedSchedules: Map<Int, List<Schedule>> = emptyMap(),
    val evaluations: List<Evaluation> = emptyList(),
    val evaluationsByDate: Map<String, List<Evaluation>> = emptyMap(),
    val dailyEvaluations: List<Evaluation> = emptyList(),
    val selectedDay: Int = (Calendar.getInstance().get(Calendar.DAY_OF_WEEK) - 1).let { if (it == 0) 0 else it },
    val selectedDate: Calendar = Calendar.getInstance(),
    val currentMonth: Calendar = Calendar.getInstance(),
    val viewMode: ScheduleViewMode = ScheduleViewMode.DAILY_TIMELINE,
    val liveClassStatus: LiveClassStatus = LiveClassStatus.NoClassesToday,
    val timelineItems: List<ScheduleTimelineItem> = emptyList(),
    val conflicts: List<Schedule> = emptyList(),
    val weeklySummary: WeeklyScheduleSummary = WeeklyScheduleSummary(),
    val firstDayOfWeek: String = "SUNDAY",
    val gradeScale: GradeScale = GradeScale.SCALE_0_10,
    val error: String? = null,
    val userFirstName: String = "",
    val userEmail: String = "",
    val userProfilePictureUrl: String? = null
)

sealed interface SchedulesEvent {
    data object LoadData : SchedulesEvent
    data object RefreshLiveStatus : SchedulesEvent
    data class SelectDay(val dayOfWeek: Int) : SchedulesEvent
    data class SelectDate(val date: Calendar) : SchedulesEvent
    data class ChangeMonth(val amount: Int) : SchedulesEvent
    data object GoToToday : SchedulesEvent
    data class SetViewMode(val mode: ScheduleViewMode) : SchedulesEvent
    data class DeleteSchedule(val schedule: Schedule) : SchedulesEvent
    data class DeleteEvaluation(val evaluation: Evaluation) : SchedulesEvent
    data object ClearError : SchedulesEvent
}
