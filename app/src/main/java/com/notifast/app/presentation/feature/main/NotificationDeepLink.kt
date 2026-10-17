package com.notifast.app.presentation.feature.main

sealed interface NotificationDeepLink {
    data object Schedules : NotificationDeepLink
    data class SubjectDetail(val subjectName: String, val evaluationId: String? = null) : NotificationDeepLink
    data object Settings : NotificationDeepLink
}
