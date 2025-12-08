package com.datavite.distrivite.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.datavite.distrivite.data.local.SyncType

@Entity(tableName = "working_periods")
data class LocalWorkingPeriod(
    @PrimaryKey val id: String,
    val created: String,
    val modified: String,
    val orgId: String,
    val orgSlug: String,
    val day: String,
    val dayId: Int,
    val isActive:Boolean,
    val startTime: String,
    val endTime: String,
    val syncType: SyncType = SyncType.UNDEFINED
)
