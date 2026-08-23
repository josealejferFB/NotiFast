package com.notifast.app.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.notifast.app.data.notification.NotificationSender
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.ScheduleRepository
import com.notifast.app.domain.repository.UserPrefsRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.util.Calendar

@HiltWorker
class DailySummaryWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val notificationSender: NotificationSender,
    private val scheduleRepository: ScheduleRepository,
    private val evaluationRepository: EvaluationRepository,
    private val authRepository: AuthRepository,
    private val userPrefsRepository: UserPrefsRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val dailySummaryEnabled = userPrefsRepository.dailySummaryEnabledFlow.first()
        if (!dailySummaryEnabled) return Result.success()

        val userId = authRepository.getCurrentUserId() ?: return Result.success()

        val cal = Calendar.getInstance()
        val todayDow = (cal.get(Calendar.DAY_OF_WEEK) - 1).let { if (it == 0) 0 else it }
        val schedules = scheduleRepository.getSchedules(userId).first().filter { it.dayOfWeek == todayDow }

        val nowMillis = System.currentTimeMillis()
        val in48hMillis = nowMillis + (48 * 60 * 60 * 1000L)

        val pendingEvals = evaluationRepository.getEvaluations(userId).first().filter { eval ->
            val evalTime = eval.date?.time ?: 0L
            evalTime in nowMillis..in48hMillis && eval.grade == null && !eval.isPresented
        }

        if (schedules.isEmpty() && pendingEvals.isEmpty()) return Result.success()

        val classesSummary = if (schedules.isNotEmpty()) "${schedules.size} clase(s) hoy" else "Sin clases hoy"
        val evalsSummary = if (pendingEvals.isNotEmpty()) "${pendingEvals.size} evaluación(es) en 48h" else "Sin entregas inminentes"
        val body = "$classesSummary • $evalsSummary"

        notificationSender.sendDailySummaryNotification(
            notificationId = 1001,
            summaryBody = body
        )

        return Result.success()
    }
}
