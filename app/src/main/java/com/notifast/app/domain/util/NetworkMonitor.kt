package com.notifast.app.domain.util

import kotlinx.coroutines.flow.Flow

interface NetworkMonitor {
    val isOnline: Flow<Boolean>
    fun isMetered(): Boolean
}
