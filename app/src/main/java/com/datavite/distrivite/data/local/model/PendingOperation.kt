package com.datavite.distrivite.data.local.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.datavite.distrivite.data.sync.EntityType
import com.datavite.distrivite.data.sync.OperationScope
import com.datavite.distrivite.data.sync.OperationType
import kotlinx.serialization.json.Json

@Entity(
    tableName = "pending_operations",
    indices = [
        Index("entityType", "entityId", "orgId"),
        Index("createdAt")
    ],
)
data class PendingOperation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val entityType: EntityType,
    val entityId: String,
    val orgSlug: String,
    val orgId: String,
    val operationType: OperationType,
    val operationScope: OperationScope,
    val payloadJson: String,
    val createdAt: Long = System.currentTimeMillis(),
    val failedAttempts: Int = 0
)
{
    // Helper function to create payload
    inline fun <reified T> parsePayload(): T {
        return Json.decodeFromString(payloadJson)
    }
}
