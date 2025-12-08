package com.datavite.distrivite.data.local.model

import androidx.room.Entity;
import androidx.room.PrimaryKey
import com.datavite.distrivite.data.local.SyncType

@Entity(tableName = "teaching_periods")
data class LocalTeachingPeriod(
    @PrimaryKey val id: String,
    val created: String,
    val modified: String,
    val orgSlug: String,
    val orgId: String,
    val start: String,
    val end: String,
    val syncType: SyncType = SyncType.UNDEFINED
)

