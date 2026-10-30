package com.notifast.app.presentation.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log

/**
 * Punto único para abrir la Política de Privacidad publicada en la landing
 * (https://notifast.pages.dev/privacy) desde la app.
 */
object PrivacyPolicyIntents {

    const val POLICY_URL = "https://notifast.pages.dev/privacy"

    fun buildPolicyIntent(): Intent =
        Intent(Intent.ACTION_VIEW, Uri.parse(POLICY_URL)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

    /** Abre la política en el navegador. Retorna false si ninguna actividad pudo resolver el enlace. */
    fun openPolicyPage(context: Context): Boolean {
        return try {
            context.startActivity(buildPolicyIntent())
            true
        } catch (e: Exception) {
            Log.w("PrivacyPolicyIntents", "Failed to open privacy policy page", e)
            false
        }
    }
}
