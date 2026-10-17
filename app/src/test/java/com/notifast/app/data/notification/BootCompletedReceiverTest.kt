package com.notifast.app.data.notification

import android.content.Context
import android.content.Intent
import com.notifast.app.domain.usecase.RescheduleAllAlertsUseCase
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

import kotlinx.coroutines.test.UnconfinedTestDispatcher

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class BootCompletedReceiverTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val rescheduleAllAlertsUseCase: RescheduleAllAlertsUseCase = mockk(relaxed = true)
    private lateinit var receiver: BootCompletedReceiver
    private lateinit var context: Context

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        context = RuntimeEnvironment.getApplication()
        receiver = BootCompletedReceiver().apply {
            markInjected(this)
            this.rescheduleAllAlertsUseCase = this@BootCompletedReceiverTest.rescheduleAllAlertsUseCase
            this.ioDispatcher = testDispatcher
        }
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
    fun `onReceive with ACTION_BOOT_COMPLETED triggers reschedule`() = runTest(testDispatcher) {
        val intent = Intent(Intent.ACTION_BOOT_COMPLETED)
        receiver.onReceive(context, intent)
        advanceUntilIdle()

        coVerify { rescheduleAllAlertsUseCase() }
    }

    @Test
    fun `onReceive with ACTION_TIME_CHANGED triggers reschedule`() = runTest(testDispatcher) {
        val intent = Intent(Intent.ACTION_TIME_CHANGED)
        receiver.onReceive(context, intent)
        advanceUntilIdle()

        coVerify { rescheduleAllAlertsUseCase() }
    }

    @Test
    fun `onReceive with ACTION_TIMEZONE_CHANGED triggers reschedule`() = runTest(testDispatcher) {
        val intent = Intent(Intent.ACTION_TIMEZONE_CHANGED)
        receiver.onReceive(context, intent)
        advanceUntilIdle()

        coVerify { rescheduleAllAlertsUseCase() }
    }

    @Test
    fun `onReceive with ACTION_MY_PACKAGE_REPLACED triggers reschedule`() = runTest(testDispatcher) {
        val intent = Intent(Intent.ACTION_MY_PACKAGE_REPLACED)
        receiver.onReceive(context, intent)
        advanceUntilIdle()

        coVerify { rescheduleAllAlertsUseCase() }
    }

    @Test
    fun `onReceive with unrelated action does not trigger reschedule`() = runTest(testDispatcher) {
        val intent = Intent("com.example.UNRELATED_ACTION")
        receiver.onReceive(context, intent)
        advanceUntilIdle()

        coVerify(exactly = 0) { rescheduleAllAlertsUseCase() }
    }
}
