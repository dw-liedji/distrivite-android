package com.datavite.distrivite.data.local.model

import androidx.room.Entity;
import androidx.room.PrimaryKey
import com.datavite.distrivite.data.local.SyncType

@Entity(tableName = "rooms")
data class LocalRoom(
    @PrimaryKey val id: String,
    val created: String,
    val modified: String,
    val orgSlug: String,
    val orgId: String,
    val name: String,
    val syncType: SyncType = SyncType.UNDEFINED
)

