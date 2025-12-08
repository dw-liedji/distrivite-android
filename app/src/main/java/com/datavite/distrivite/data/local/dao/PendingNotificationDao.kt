package com.datavite.distrivite.data.local.dao

import androidx.room.*
import com.datavite.distrivite.data.local.model.NotificationStatus
import com.datavite.distrivite.data.local.model.PendingNotificationAction

@Dao
interface PendingNotificationDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(action: PendingNotificationAction)

    @Query("SELECT * FROM pending_notification_actions WHERE sessionId = :sessionId LIMIT 1")
    suspend fun getPendingForSession(sessionId: String): PendingNotificationAction?

    @Query("SELECT * FROM pending_notification_actions WHERE status = :status")
    suspend fun getAllWithStatus(status: NotificationStatus): List<PendingNotificationAction>

    @Query("UPDATE pending_notification_actions SET status = :status WHERE sessionId = :sessionId")
    suspend fun updateStatus(sessionId: String, status: NotificationStatus)

    @Delete
    suspend fun delete(action: PendingNotificationAction)
}
