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

private const val TAG = "BootCompletedReceiver"

@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var rescheduleAllAlertsUseCase: RescheduleAllAlertsUseCase

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    rescheduleAllAlertsUseCase()
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to reschedule alerts after boot", e)
                } finally {
                    try {
                        pendingResult.finish()
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to finish broadcast pending result", e)
                    }
                }
            }
        }
    }
}
