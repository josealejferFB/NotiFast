package com.notifast.app.presentation.atoms

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.notifast.app.R
import com.notifast.app.presentation.atoms.header.AgendaTopBarContent
import com.notifast.app.presentation.atoms.header.DetailTopBarContent
import com.notifast.app.presentation.atoms.header.GradesTopBarContent
import com.notifast.app.presentation.atoms.header.HomeTopBarContent
import com.notifast.app.presentation.atoms.header.SettingsTopBarContent
import com.notifast.app.presentation.atoms.header.SimpleTopBarContent
import com.notifast.app.presentation.atoms.header.StudyTopBarContent
import java.util.Locale

/**
 * NotiFast 3.2 AAA Editorial Header Orchestrator.
 * Implements the Tactile Studio Notebook aesthetic: high-contrast typography,
 * spring-physics tactile response, WCAG AAA accessibility, and zero cognitive redundancy.
 */
@Composable
fun NotiFastTopBar(
    type: TopBarType,
    modifier: Modifier = Modifier
) {
    val defaultStudentName = stringResource(R.string.topbar_default_student)
    val homeDisplayName = remember(type, defaultStudentName) {
        if (type is TopBarType.Home) {
            if (type.firstName.isNotBlank()) {
                type.firstName.trim()
            } else {
                type.email.split("@").getOrElse(0) { defaultStudentName }
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
            }
        } else ""
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
    ) {
        when (type) {
            is TopBarType.Home -> {
                HomeTopBarContent(
                    displayName = homeDisplayName,
                    type = type
                )
            }
            is TopBarType.Agenda -> {
                AgendaTopBarContent(type = type)
            }
            is TopBarType.Grades -> {
                GradesTopBarContent(type = type)
            }
            is TopBarType.Study -> {
                StudyTopBarContent(type = type)
            }
            is TopBarType.Settings -> {
                SettingsTopBarContent(type = type)
            }
            is TopBarType.Detail -> {
                DetailTopBarContent(type = type)
            }
            is TopBarType.Simple -> {
                SimpleTopBarContent(type = type)
            }
        }
    }
}
