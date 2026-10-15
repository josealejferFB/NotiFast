package com.notifast.app.domain.model.cloud

data class ItemOutcome(
    val id: String,
    val name: String,
    val error: Throwable? = null
)

data class BatchResult(
    val outcomes: List<ItemOutcome>
) {
    val failed: List<ItemOutcome> get() = outcomes.filter { it.error != null }
    val isAllSuccessful: Boolean get() = failed.isEmpty()
}

sealed interface UploadProgress {
    data class Progress(val done: Int, val total: Int, val current: String) : UploadProgress
    data class Done(val result: BatchResult) : UploadProgress
}
