package com.notifast.app.domain.model.cloud

data class ItemOutcome(
    val id: String,
    val name: String,
    val error: Throwable? = null,
    val isNoOp: Boolean = false
)

data class BatchResult(
    val outcomes: List<ItemOutcome>
) {
    val failed: List<ItemOutcome> get() = outcomes.filter { it.error != null }
    val noOpCount: Int get() = outcomes.count { it.isNoOp }
    val actualSuccessCount: Int get() = outcomes.count { it.error == null && !it.isNoOp }
    val isAllSuccessful: Boolean get() = failed.isEmpty()
}

sealed interface UploadProgress {
    data class Progress(val done: Int, val total: Int, val current: String) : UploadProgress
    data class Done(val result: BatchResult) : UploadProgress
}
