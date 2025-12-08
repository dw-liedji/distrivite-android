package com.datavite.distrivite.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_notification_actions")
data class PendingNotificationAction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: String,
    val status: NotificationStatus = NotificationStatus.PENDING
)

enum class NotificationStatus {
    PENDING,
    SENT,
    FAILED
}
