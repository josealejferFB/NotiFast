package com.notifast.app.data.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.notifast.app.domain.usecase.RescheduleAllAlertsUseCase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class BootCompletedReceiver : BroadcastReceiver() {

    @Inject
    lateinit var rescheduleAllAlertsUseCase: RescheduleAllAlertsUseCase

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED || intent.action == "android.intent.action.QUICKBOOT_POWERON") {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    rescheduleAllAlertsUseCase()
                } catch (e: Exception) {
                    // Log y captura segura de error para no bloquear receiver
                }
            }
        }
    }
}
