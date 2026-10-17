package com.notifast.app.data.notification

import android.content.Context
import android.content.Intent
import com.notifast.app.data.model.Evaluation
import com.notifast.app.data.model.Schedule
import com.notifast.app.domain.repository.AuthRepository
import com.notifast.app.domain.repository.EvaluationRepository
import com.notifast.app.domain.repository.ScheduleRepository
import com.notifast.app.domain.repository.UserPrefsRepository
import com.notifast.app.domain.usecase.ScheduleClassAlertUseCase
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AlarmTriggerReceiverTest {

    private val testDispatcher = kotlinx.coroutines.test.UnconfinedTestDispatcher()
    private val notificationSender: NotificationSender = mockk(relaxed = true)
    private val scheduleRepository: ScheduleRepository = mockk(relaxed = true)
    private val evaluationRepository: EvaluationRepository = mockk(relaxed = true)
    private val userPrefsRepository: UserPrefsRepository = mockk(relaxed = true)
    private val authRepository: AuthRepository = mockk(relaxed = true)
    private val scheduleClassAlertUseCase: ScheduleClassAlertUseCase = mockk(relaxed = true)

    private lateinit var receiver: AlarmTriggerReceiver
    private lateinit var context: Context

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = RuntimeEnvironment.getApplication()
        receiver = AlarmTriggerReceiver().apply {
            markInjected(this)
            this.notificationSender = this@AlarmTriggerReceiverTest.notificationSender
            this.scheduleRepository = this@AlarmTriggerReceiverTest.scheduleRepository
            this.evaluationRepository = this@AlarmTriggerReceiverTest.evaluationRepository
            this.userPrefsRepository = this@AlarmTriggerReceiverTest.userPrefsRepository
            this.authRepository = this@AlarmTriggerReceiverTest.authRepository
            this.scheduleClassAlertUseCase = this@AlarmTriggerReceiverTest.scheduleClassAlertUseCase
            this.ioDispatcher = testDispatcher
        }
        coEvery { authRepository.getCurrentUserId() } returns "user_123"
        every { userPrefsRepository.classAlertsEnabledFlow } returns flowOf(true)
        every { userPrefsRepository.evalAlertsEnabledFlow } returns flowOf(true)
    }

    private fun markInjected(target: Any) {
        var cls: Class<*>? = target.javaClass
        while (cls != null) {
            try {
                val field = cls.getDeclaredField("injected")
                field.isAccessible = true
                field.set(target, true)
                return
            } catch (_: NoSuchFieldException) {
                cls = cls.superclass
            }
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `handleClassAlert re-arms schedule and sends class notification`() = runTest(testDispatcher) {
        val schedule = Schedule(id = "sch_1", subjectName = "Redes", classroom = "A-12", startTime = "10:00")
        every { scheduleRepository.getSchedules("user_123") } returns flowOf(listOf(schedule))

        val intent = Intent(AlarmTriggerReceiver.ACTION_CLASS_ALERT).apply {
            putExtra(AlarmTriggerReceiver.EXTRA_SCHEDULE_ID, "sch_1")
            putExtra(AlarmTriggerReceiver.EXTRA_SUBJECT_NAME, "Redes")
            putExtra(AlarmTriggerReceiver.EXTRA_CLASSROOM, "A-12")
            putExtra(AlarmTriggerReceiver.EXTRA_START_TIME, "10:00")
        }

        receiver.onReceive(context, intent)
        advanceUntilIdle()

        coVerify { scheduleClassAlertUseCase(schedule) }
        verify {
            notificationSender.sendClassNotification(
                notificationId = "sch_1".hashCode(),
                subjectName = "Redes",
                classroom = "A-12",
                startTime = "10:00"
            )
        }
    }

    @Test
    fun `handleEvaluationAlert sends notification when evaluation is active`() = runTest(testDispatcher) {
        val evaluation = Evaluation(
            id = "eval_1",
            subjectName = "Sistemas",
            title = "Parcial 1",
            grade = null,
            isPresented = false
        )
        every { evaluationRepository.getEvaluation("user_123", "eval_1") } returns flowOf(evaluation)

        val intent = Intent(AlarmTriggerReceiver.ACTION_EVALUATION_ALERT).apply {
            putExtra(AlarmTriggerReceiver.EXTRA_EVALUATION_ID, "eval_1")
            putExtra(AlarmTriggerReceiver.EXTRA_SUBJECT_NAME, "Sistemas")
            putExtra(AlarmTriggerReceiver.EXTRA_TITLE, "Parcial 1")
            putExtra(AlarmTriggerReceiver.EXTRA_DATE, "15/11/2026")
            putExtra(AlarmTriggerReceiver.EXTRA_WEIGHT, 30.0)
        }

        receiver.onReceive(context, intent)
        advanceUntilIdle()

        verify {
            notificationSender.sendEvaluationNotification(
                notificationId = "eval_1".hashCode(),
                subjectName = "Sistemas",
                evalTitle = "Parcial 1",
                dateText = "15/11/2026",
                weight = 30.0,
                evaluationId = "eval_1"
            )
        }
    }

    @Test
    fun `handleEvaluationAlert suppresses notification when already graded`() = runTest(testDispatcher) {
        val evaluation = Evaluation(
            id = "eval_graded",
            subjectName = "Sistemas",
            title = "Parcial 1",
            grade = 18.0,
            isPresented = true
        )
        every { evaluationRepository.getEvaluation("user_123", "eval_graded") } returns flowOf(evaluation)

        val intent = Intent(AlarmTriggerReceiver.ACTION_EVALUATION_ALERT).apply {
            putExtra(AlarmTriggerReceiver.EXTRA_EVALUATION_ID, "eval_graded")
            putExtra(AlarmTriggerReceiver.EXTRA_SUBJECT_NAME, "Sistemas")
            putExtra(AlarmTriggerReceiver.EXTRA_TITLE, "Parcial 1")
            putExtra(AlarmTriggerReceiver.EXTRA_DATE, "15/11/2026")
            putExtra(AlarmTriggerReceiver.EXTRA_WEIGHT, 30.0)
        }

        receiver.onReceive(context, intent)
        advanceUntilIdle()

        verify(exactly = 0) {
            notificationSender.sendEvaluationNotification(any(), any(), any(), any(), any(), any())
        }
    }
}
