package com.notifast.app.presentation.feature.grades

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.notifast.app.R
import com.notifast.app.presentation.atoms.NotiFastListSkeleton
import com.notifast.app.presentation.atoms.NotiFastStudyBackground
import com.notifast.app.presentation.atoms.NotiFastTopBar
import com.notifast.app.presentation.atoms.TopBarType
import com.notifast.app.presentation.feature.grades.components.AcademicAnalyticsContent
import com.notifast.app.presentation.feature.grades.components.EmptyGradesState
import com.notifast.app.presentation.feature.grades.components.GradesTabSelector
import com.notifast.app.presentation.feature.grades.components.MainAverageDashboard
import com.notifast.app.presentation.feature.grades.components.SubjectGradeCard

/**
 * NotiFast 3.0 Grades and Predictive Analytics Screen.
 * Fully modularized following Clean Architecture and SRP (< 140 LOC).
 */
@Composable
fun GradesScreen(
    viewModel: GradesViewModel = hiltViewModel(),
    onEditProfile: () -> Unit = {},
    onSearchClick: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        NotiFastStudyBackground()
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                NotiFastTopBar(
                    type = TopBarType.Home(
                        firstName = uiState.userFirstName,
                        email = uiState.userEmail,
                        profilePictureUrl = uiState.userProfilePictureUrl,
                        contextText = stringResource(R.string.grades_title),
                        onProfileClick = onEditProfile,
                        onSearchClick = onSearchClick
                    )
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .safeDrawingPadding()
            ) {
                if (uiState.isLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Spacer(Modifier.height(8.dp))
                        NotiFastListSkeleton(itemCount = 4)
                    }
                    return@Scaffold
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp)
                ) {
                    if (!uiState.error.isNullOrBlank()) {
                        item {
                            com.notifast.app.presentation.atoms.NotiFastAlertBanner(
                                message = uiState.error ?: "",
                                onDismiss = { viewModel.onEvent(GradesEvent.ClearError) }
                            )
                        }
                    }

                    item {
                        GradesTabSelector(
                            selectedTab = uiState.selectedTab,
                            onTabSelected = { viewModel.onEvent(GradesEvent.SelectTab(it)) }
                        )
                    }

                    when (uiState.selectedTab) {
                        GradesTab.SUBJECTS -> {
                            item {
                                MainAverageDashboard(
                                    generalAverage = uiState.globalAverage,
                                    gradeScale = uiState.gradeScale,
                                    minPassingGrade = uiState.minPassingGrade
                                )
                            }

                            if (uiState.subjectGrades.isEmpty()) {
                                item {
                                    EmptyGradesState()
                                }
                            } else {
                                items(uiState.subjectGrades, key = { it.subject }) { subjectGrade ->
                                    SubjectGradeCard(
                                        subjectGrade = subjectGrade,
                                        onUpdateGrade = { id, g, p ->
                                            viewModel.onEvent(GradesEvent.UpdateGrade(id, g, p))
                                        },
                                        displayScale = uiState.gradeScale,
                                        minPassingGrade = uiState.minPassingGrade
                                    )
                                }
                            }
                        }
                        GradesTab.ANALYTICS -> {
                            item {
                                AcademicAnalyticsContent(
                                    stats = uiState.academicStats,
                                    gradeScale = uiState.gradeScale,
                                    minPassingGrade = uiState.minPassingGrade
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
