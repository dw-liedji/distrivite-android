package com.datavite.distrivite.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.datavite.distrivite.data.local.SyncType

@Entity(tableName = "organization_users")
data class LocalOrganizationUser(
    @PrimaryKey val id: String,
    val created: Long,
    val modified: Long,
    val name: String,
    val orgSlug: String,
    val orgId: String,
    val userId: String,
    val isAdmin: Boolean,
    val isActive: Boolean,
    val embeddings: List<List<Float>>,
    val syncType: SyncType = SyncType.UNDEFINED
)
