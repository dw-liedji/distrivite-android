package com.datavite.distrivite.data.local.model

import androidx.room.Entity
import com.datavite.distrivite.data.sync.EntityType
import com.datavite.distrivite.data.sync.OperationType
import kotlinx.serialization.json.Json

@Entity(
    tableName = "pending_operations",
    primaryKeys = ["entityType", "entityId", "orgId", "operationType"]
)
data class PendingOperation(
    val entityType: EntityType,
    val entityId: String,
    val orgSlug: String,
    val orgId: String,
    val operationType: OperationType,
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
