package com.notifast.app.presentation.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class PrivacyPolicyIntentsTest {

    @Test
    fun `buildPolicyIntent usa ACTION_VIEW con la url de la politica`() {
        val intent = PrivacyPolicyIntents.buildPolicyIntent()

        assertEquals(Intent.ACTION_VIEW, intent.action)
        assertEquals(Uri.parse(PrivacyPolicyIntents.POLICY_URL), intent.data)
    }

    @Test
    fun `openPolicyPage lanza el navegador con la politica`() {
        val app = RuntimeEnvironment.getApplication()

        val launched = PrivacyPolicyIntents.openPolicyPage(app)

        assertTrue(launched)
        val started = shadowOf(app).nextStartedActivity
        assertNotNull(started)
        assertEquals(Intent.ACTION_VIEW, started.action)
        assertEquals(Uri.parse(PrivacyPolicyIntents.POLICY_URL), started.data)
    }

    @Test
    fun `openPolicyPage retorna false si ninguna actividad puede abrir el enlace`() {
        val context = mockk<Context>()
        every { context.startActivity(any()) } throws android.content.ActivityNotFoundException("sin navegador")

        val launched = PrivacyPolicyIntents.openPolicyPage(context)

        assertFalse(launched)
    }
}
