package com.notifast.app.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.notifast.app.domain.usecase.RescheduleAllAlertsUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.notifast.app.di.IoDispatcher
import kotlinx.coroutines.CoroutineDispatcher

private const val TAG = "BootCompletedReceiver"

@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var rescheduleAllAlertsUseCase: RescheduleAllAlertsUseCase

    @Inject
    @IoDispatcher
    lateinit var ioDispatcher: CoroutineDispatcher

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == "android.intent.action.QUICKBOOT_POWERON" ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == Intent.ACTION_TIME_CHANGED ||
            action == Intent.ACTION_TIMEZONE_CHANGED
        ) {
            val pendingResult = goAsync()
            CoroutineScope(ioDispatcher).launch {
                try {
                    rescheduleAllAlertsUseCase()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to reschedule alerts after boot", e)
                } finally {
                    try {
                        pendingResult?.finish()
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to finish broadcast pending result", e)
                    }
                }
            }
        }
    }
}
