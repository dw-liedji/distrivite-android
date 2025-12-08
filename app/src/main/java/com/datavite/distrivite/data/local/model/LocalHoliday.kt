package com.datavite.distrivite.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.datavite.distrivite.data.local.SyncType
import kotlinx.serialization.Serializable

@Entity(tableName = "holidays")
@Serializable
data class LocalHoliday(
    @PrimaryKey val id: String,
    val created: String,
    val modified: String,
    val orgId: String,
    val orgSlug: String,
    val name: String,
    val date: String,
    val type: String,
    val syncType: SyncType = SyncType.UNDEFINED
)
