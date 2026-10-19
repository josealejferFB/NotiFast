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
        assertNull(defaultHome.contextIcon)

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

    @Test
    fun `agenda top bar contract preserves dateText and search action`() {
        var searchClicked = false
        val agenda = TopBarType.Agenda(
            title = "Agenda Académica",
            dateText = "Hoy, Lunes",
            onSearchClick = { searchClicked = true }
        )

        assertEquals("Agenda Académica", agenda.title)
        assertEquals("Hoy, Lunes", agenda.dateText)
        agenda.onSearchClick?.invoke()
        assertTrue(searchClicked)
    }

    @Test
    fun `grades top bar contract preserves averageGrade and gradeScale`() {
        var searchClicked = false
        val grades = TopBarType.Grades(
            title = "Calificaciones",
            averageGrade = 9.25,
            gradeScale = com.notifast.app.domain.model.GradeScale.SCALE_0_10,
            onSearchClick = { searchClicked = true }
        )

        assertEquals("Calificaciones", grades.title)
        assertEquals(9.25, grades.averageGrade ?: 0.0, 0.001)
        assertEquals(com.notifast.app.domain.model.GradeScale.SCALE_0_10, grades.gradeScale)
        grades.onSearchClick?.invoke()
        assertTrue(searchClicked)
    }

    @Test
    fun `study top bar contract preserves documentCount and draftCount`() {
        var searchClicked = false
        val study = TopBarType.Study(
            title = "Estudio & Portadas",
            documentCount = 14,
            draftCount = 3,
            onSearchClick = { searchClicked = true }
        )

        assertEquals("Estudio & Portadas", study.title)
        assertEquals(14, study.documentCount)
        assertEquals(3, study.draftCount)
        study.onSearchClick?.invoke()
        assertTrue(searchClicked)
    }

    @Test
    fun `settings top bar contract preserves syncSummary and cloud action`() {
        var cloudClicked = false
        var searchClicked = false
        val settings = TopBarType.Settings(
            title = "Configuración",
            syncSummary = LocalSyncSummary.ALL_SYNCED,
            accountEmail = "estudiante@universidad.edu",
            onCloudClick = { cloudClicked = true },
            onSearchClick = { searchClicked = true }
        )

        assertEquals("Configuración", settings.title)
        assertEquals(LocalSyncSummary.ALL_SYNCED, settings.syncSummary)
        assertEquals("estudiante@universidad.edu", settings.accountEmail)
        settings.onCloudClick?.invoke()
        assertTrue(cloudClicked)
        settings.onSearchClick?.invoke()
        assertTrue(searchClicked)
    }
}

