package com.datavite.distrivite.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "localCustomers")
data class LocalCustomer(
    @PrimaryKey val id: String,
    val created: String,
    val modified: String,
    val orgSlug: String,
    val orgId: String,
    val name: String,
    val phoneNumber: String?,
    val syncStatus: SyncStatus)