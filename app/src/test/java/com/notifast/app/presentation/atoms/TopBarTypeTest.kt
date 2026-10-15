package com.notifast.app.presentation.atoms

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

import com.notifast.app.domain.model.cloud.LocalSyncSummary

/**
 * Unit tests for TopBarType model and contracts.
 */
class TopBarTypeTest {

    @Test
    fun `home top bar contract preserves properties correctly`() {
        var profileClicked = false
        var notificationsClicked = false
        var cloudClicked = false

        val home = TopBarType.Home(
            firstName = "Carlos",
            email = "carlos@universidad.edu",
            profilePictureUrl = "https://example.com/avatar.jpg",
            hasUnreadNotifications = true,
            syncSummary = LocalSyncSummary.PENDING,
            contextText = "Semestre 2026-II",
            onProfileClick = { profileClicked = true },
            onNotificationsClick = { notificationsClicked = true },
            onCloudClick = { cloudClicked = true }
        )

        assertEquals("Carlos", home.firstName)
        assertEquals("carlos@universidad.edu", home.email)
        assertEquals("https://example.com/avatar.jpg", home.profilePictureUrl)
        assertTrue(home.hasUnreadNotifications)
        assertEquals(LocalSyncSummary.PENDING, home.syncSummary)
        assertEquals("Semestre 2026-II", home.contextText)

        home.onProfileClick()
        assertTrue(profileClicked)

        home.onNotificationsClick()
        assertTrue(notificationsClicked)

        home.onCloudClick()
        assertTrue(cloudClicked)
    }

    @Test
    fun `home top bar contract defaults to NO_ACCOUNT and allows ALL_SYNCED`() {
        val defaultHome = TopBarType.Home(
            firstName = "Ana",
            email = "ana@universidad.edu",
            profilePictureUrl = null,
            onProfileClick = {}
        )
        assertEquals(LocalSyncSummary.NO_ACCOUNT, defaultHome.syncSummary)

        val syncedHome = defaultHome.copy(syncSummary = LocalSyncSummary.ALL_SYNCED)
        assertEquals(LocalSyncSummary.ALL_SYNCED, syncedHome.syncSummary)
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
