package com.datavite.distrivite.domain.notification

import kotlinx.coroutines.flow.Flow

interface NotificationBus {
    val events: Flow<NotificationEvent>
    suspend fun emit(event: NotificationEvent)
}
