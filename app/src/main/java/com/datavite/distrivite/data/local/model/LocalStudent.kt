package com.datavite.distrivite.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.datavite.distrivite.data.local.SyncType

@Entity(tableName = "localStudents")
data class LocalStudent(
    @PrimaryKey val id: String,
    val created: String,
    val modified: String,
    val name: String,
    val orgSlug: String,
    val orgId: String,
    val userId: String,
    val isActive:Boolean,
    val hasMailingTracking:Boolean,
    val hasSmsTracking:Boolean,
    val isDelegate:Boolean,
    val educationClass: String,
    val educationClassId: String,
    val embeddings: List<List<Float>>,
    val syncType: SyncType = SyncType.UNDEFINED
)
