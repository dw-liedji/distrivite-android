package com.datavite.distrivite.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.datavite.distrivite.data.local.SyncType

@Entity(tableName = "leaves")
data class LocalLeave(
    @PrimaryKey val id: String,
    val created: String,
    val modified: String,
    val orgId: String,
    val userId: String,
    val orgUserId:String,
    val orgSlug: String,
    val employeeId: String,
    val employeeName: String,
    val type: String,
    val hourlySalary: Double,
    val startDate: String,
    val endDate: String,
    val status: String,
    val reason:String,
    val syncType: SyncType = SyncType.UNDEFINED
)
