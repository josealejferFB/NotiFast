package com.notifast.app.presentation.atoms

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for TopBarType model and contracts.
 */
class TopBarTypeTest {

    @Test
    fun `home top bar contract preserves properties correctly`() {
        var profileClicked = false
        var notificationsClicked = false

        val home = TopBarType.Home(
            firstName = "Carlos",
            email = "carlos@universidad.edu",
            profilePictureUrl = "https://example.com/avatar.jpg",
            hasUnreadNotifications = true,
            isCloudSynced = false,
            contextText = "Semestre 2026-II",
            onProfileClick = { profileClicked = true },
            onNotificationsClick = { notificationsClicked = true }
        )

        assertEquals("Carlos", home.firstName)
        assertEquals("carlos@universidad.edu", home.email)
        assertEquals("https://example.com/avatar.jpg", home.profilePictureUrl)
        assertTrue(home.hasUnreadNotifications)
        assertFalse(home.isCloudSynced)
        assertEquals("Semestre 2026-II", home.contextText)

        home.onProfileClick()
        assertTrue(profileClicked)

        home.onNotificationsClick()
        assertTrue(notificationsClicked)
    }

    @Test
    fun `home top bar contract preserves isCloudSynced false for local offline storage`() {
        val defaultHome = TopBarType.Home(
            firstName = "Ana",
            email = "ana@universidad.edu",
            profilePictureUrl = null,
            onProfileClick = {}
        )
        assertTrue(defaultHome.isCloudSynced)

        val offlineHome = defaultHome.copy(isCloudSynced = false)
        assertFalse(offlineHome.isCloudSynced)
    }

    @Test
    fun `detail top bar contract supports subtitle and back action`() {
        var backClicked = false

        val detail = TopBarType.Detail(
            title = "Editar Apunte",
            subtitle = "Cálculo Multivariable",
            onBackClick = { backClicked = true }
        )

        assertEquals("Editar Apunte", detail.title)
        assertEquals("Cálculo Multivariable", detail.subtitle)

        detail.onBackClick()
        assertTrue(backClicked)
    }

    @Test
    fun `simple top bar contract preserves title and optional subtitle`() {
        val simple = TopBarType.Simple(
            title = "Mis Horarios",
            subtitle = null
        )

        assertEquals("Mis Horarios", simple.title)
        assertNull(simple.subtitle)
    }
}
