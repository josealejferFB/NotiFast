package com.example.notifast.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Notes : Screen("notes")
    object AddNote : Screen("add_note")
    object AddSubject : Screen("add_subject")
    object EditNote : Screen("edit_note")
    object EditProfile : Screen("edit_profile")
    object SubjectDetail : Screen("subject_detail/{subjectName}") {
        fun createRoute(subjectName: String) = "subject_detail/$subjectName"
    }
}
