package com.datavite.distrivite.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.datavite.distrivite.data.local.SyncType

@Entity(tableName = "localInstructorContracts")
data class LocalInstructorContract(
    @PrimaryKey val id: String,
    val created: Long,
    val modified: Long,
    val name: String,
    val contract: String,
    val orgSlug: String,
    val orgId: String,
    val instructorId: String,
    val userId: String,
    val orgUserId:String,
    val isManager: Boolean,
    val isActive: Boolean,
    val checkInLatitude:Double,
    val checkInLongitude:Double,
    val checkOutLatitude:Double,
    val checkOutLongitude:Double,
    val embeddings: List<List<Float>>,
    val syncType: SyncType = SyncType.UNDEFINED
)
