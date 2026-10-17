package com.notifast.app.domain.usecase

import androidx.work.WorkManager
import com.notifast.app.data.model.Schedule
import com.notifast.app.domain.notification.ExactAlarmScheduler
import com.notifast.app.domain.repository.UserPrefsRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class ScheduleClassAlertUseCaseTest {

    private val exactAlarmScheduler: ExactAlarmScheduler = mockk(relaxed = true)
    private val workManager: WorkManager = mockk(relaxed = true)
    private val userPrefsRepository: UserPrefsRepository = mockk(relaxed = true)

    private lateinit var useCase: ScheduleClassAlertUseCase

    @Before
    fun setUp() {
        every { userPrefsRepository.classAlertsEnabledFlow } returns flowOf(true)
        every { userPrefsRepository.classAlertMinutesFlow } returns flowOf(15)
        useCase = ScheduleClassAlertUseCase(exactAlarmScheduler, workManager, userPrefsRepository)
    }

    @Test
    fun `when schedule provided, schedules exact alarm and cancels legacy work`() = runTest {
        val schedule = Schedule(
            id = "sch_123",
            subjectName = "Algoritmos",
            dayOfWeek = 1,
            startTime = "08:00",
            endTime = "10:00",
            classroom = "Lab 4"
        )

        useCase(schedule)

        verify {
            exactAlarmScheduler.scheduleClassAlarm(
                scheduleId = "sch_123",
                subjectName = "Algoritmos",
                classroom = "Lab 4",
                startTime = "08:00",
                triggerAtMillis = any()
            )
            workManager.cancelUniqueWork("class_sch_123")
        }
    }

    @Test
    fun `when class alerts disabled in preferences, does not schedule alarm`() = runTest {
        every { userPrefsRepository.classAlertsEnabledFlow } returns flowOf(false)
        val schedule = Schedule(
            id = "sch_456",
            subjectName = "Historia",
            dayOfWeek = 2,
            startTime = "10:00",
            endTime = "12:00"
        )

        useCase(schedule)

        verify(exactly = 0) {
            exactAlarmScheduler.scheduleClassAlarm(any(), any(), any(), any(), any())
        }
    }
}
