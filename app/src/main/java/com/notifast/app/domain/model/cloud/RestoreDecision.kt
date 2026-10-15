package com.notifast.app.domain.model.cloud

data class RestoreConflict(
    val item: CloudSubjectItem,
    val localSubjectId: String,
    val localName: String,
    val hasLocalChanges: Boolean,
    val suggestedName: String
)

sealed interface RestoreDecision {
    data object Overwrite : RestoreDecision
    data class AddAs(val name: String) : RestoreDecision
    data object Skip : RestoreDecision
}
