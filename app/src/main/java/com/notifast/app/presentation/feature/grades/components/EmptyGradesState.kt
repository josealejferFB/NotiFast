package com.notifast.app.presentation.feature.grades.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.School
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.notifast.app.R
import com.notifast.app.presentation.atoms.NotiFastEmptyState

/**
 * Empty state for Grades screen using NotiFast 3.0 empty state atom.
 */
@Composable
fun EmptyGradesState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp)
    ) {
        NotiFastEmptyState(
            icon = Icons.Rounded.School,
            title = stringResource(R.string.grades_empty_no_evaluations),
            message = stringResource(R.string.grades_empty_message)
        )
    }
}
