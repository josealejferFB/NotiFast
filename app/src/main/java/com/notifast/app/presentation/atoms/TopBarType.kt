package com.notifast.app.presentation.atoms

import androidx.compose.runtime.Composable
import com.notifast.app.domain.model.GradeScale
import com.notifast.app.domain.model.cloud.LocalSyncSummary

/**
 * NotiFast 3.2 AAA Editorial TopBar Types.
 * Encapsulates the visual and contextual hierarchy of each screen:
 * - [Home]: Student Hub (Notes) with tactile avatar, greeting, date pill, and full actions.
 * - [Agenda]: Academic Schedule with temporal context pill and contextual actions.
 * - [Grades]: Academic Performance with JetBrains Mono tabular GPA / Scale micro-badge.
 * - [Study]: Study & Workshop Hub with document/draft counters and tools.
 * - [Settings]: Serene Configuration Hub with Google Drive honest sync status.
 * - [Detail]: Secondary & Modal Views with tactile spring back button (48x48dp target).
 * - [Simple]: Lightweight header with title and action slot.
 */
sealed class TopBarType {

    /**
     * Primary student home hub (Notes).
     */
    data class Home(
        val firstName: String,
        val email: String,
        val profilePictureUrl: String?,
        val hasUnreadNotifications: Boolean = false,
        val syncSummary: LocalSyncSummary = LocalSyncSummary.NO_ACCOUNT,
        val contextText: String? = null,
        val contextIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
        val onProfileClick: () -> Unit,
        val onNotificationsClick: () -> Unit = {},
        val onSearchClick: (() -> Unit)? = null,
        val onCloudClick: () -> Unit = {}
    ) : TopBarType()

    /**
     * Academic Agenda & Schedules screen with temporal focus.
     */
    data class Agenda(
        val title: String? = null,
        val dateText: String? = null,
        val onSearchClick: (() -> Unit)? = null,
        val actions: (@Composable () -> Unit)? = null
    ) : TopBarType()

    /**
     * Academic Performance and predictive analytics with tabular GPA micro-badge.
     */
    data class Grades(
        val title: String? = null,
        val averageGrade: Double? = null,
        val gradeScale: GradeScale? = null,
        val onSearchClick: (() -> Unit)? = null,
        val actions: (@Composable () -> Unit)? = null
    ) : TopBarType()

    /**
     * Study workshop & document hub with draft and document count badges.
     */
    data class Study(
        val title: String? = null,
        val documentCount: Int = 0,
        val draftCount: Int = 0,
        val onSearchClick: (() -> Unit)? = null,
        val actions: (@Composable () -> Unit)? = null
    ) : TopBarType()

    /**
     * Configuration & Settings with Google Drive status indicator.
     */
    data class Settings(
        val title: String? = null,
        val syncSummary: LocalSyncSummary = LocalSyncSummary.NO_ACCOUNT,
        val accountEmail: String? = null,
        val onCloudClick: (() -> Unit)? = null,
        val onSearchClick: (() -> Unit)? = null,
        val actions: (@Composable () -> Unit)? = null
    ) : TopBarType()

    /**
     * Detail and editing view with tactile back button and optional action slot.
     */
    data class Detail(
        val title: String,
        val subtitle: String? = null,
        val onBackClick: () -> Unit,
        val actions: @Composable () -> Unit = {}
    ) : TopBarType()

    /**
     * Minimalist simple view with title, optional subtitle, and actions.
     */
    data class Simple(
        val title: String,
        val subtitle: String? = null,
        val actions: @Composable () -> Unit = {}
    ) : TopBarType()
}
