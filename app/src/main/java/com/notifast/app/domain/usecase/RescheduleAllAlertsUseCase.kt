package com.notifast.app.domain.usecase

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.notifast.app.data.worker.DailySummaryWorker
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.ScheduleRepository
import com.notifast.app.domain.repository.UserPrefsRepository
import kotlinx.coroutines.flow.first
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class RescheduleAllAlertsUseCase @Inject constructor(
    private val workManager: WorkManager,
    private val authRepository: AuthRepository,
    private val evaluationRepository: EvaluationRepository,
    private val scheduleRepository: ScheduleRepository,
    private val userPrefsRepository: UserPrefsRepository,
    private val scheduleEvaluationAlertUseCase: ScheduleEvaluationAlertUseCase,
    private val scheduleClassAlertUseCase: ScheduleClassAlertUseCase
) {
    suspend operator fun invoke() {
        val userId = authRepository.getCurrentUserId() ?: return

        // 1. Sincronizar evaluaciones activas
        val evaluations = evaluationRepository.getEvaluations(userId).first()
        evaluations.filter { it.grade == null && !it.isPresented }.forEach { eval ->
            scheduleEvaluationAlertUseCase(eval)
        }

        // 2. Sincronizar horarios de clases
        val schedules = scheduleRepository.getSchedules(userId).first()
        schedules.forEach { sch ->
            scheduleClassAlertUseCase(sch)
        }

        // 3. Resumen diario periódico si está activo
        val dailySummaryEnabled = userPrefsRepository.dailySummaryEnabledFlow.first()
        if (dailySummaryEnabled) {
            val initialDelay = calculateDailySummaryInitialDelay()
            val summaryRequest = PeriodicWorkRequestBuilder<DailySummaryWorker>(1, TimeUnit.DAYS)
                .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                .addTag("daily_summary")
                .build()

            workManager.enqueueUniquePeriodicWork(
                "daily_summary",
                ExistingPeriodicWorkPolicy.UPDATE,
                summaryRequest
            )
        } else {
            workManager.cancelUniqueWork("daily_summary")
        }
    }

    private fun calculateDailySummaryInitialDelay(): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 7)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) {
                add(Calendar.DAY_OF_YEAR, 1)
            }
        }
        return target.timeInMillis - now.timeInMillis
    }
}
