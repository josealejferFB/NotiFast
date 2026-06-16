package com.example.notifast.presentation.feature.editprofile

import com.example.notifast.data.model.User

data class EditProfileUiState(
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val userProfile: User? = null,
    val firstName: String = "",
    val lastName: String = ""
)

sealed interface EditProfileEvent {
    data object LoadProfile : EditProfileEvent
    data class UpdateFirstName(val value: String) : EditProfileEvent
    data class UpdateLastName(val value: String) : EditProfileEvent
    data class SaveProfile(val imageUri: android.net.Uri?) : EditProfileEvent
}
