package com.datavite.distrivite.domain.notification

sealed class NotificationEvent(open val message: String) {
    data class Success(override val message: String) : NotificationEvent(message)
    data class Failure(override val message: String) : NotificationEvent(message)
    data class Error(override val message: String) : NotificationEvent(message)
}

