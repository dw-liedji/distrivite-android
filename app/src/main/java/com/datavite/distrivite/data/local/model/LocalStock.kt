package com.datavite.distrivite.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "localStocks")
data class LocalStock(
    @PrimaryKey val id: String,
    val created: String,
    val modified: String,
    val orgSlug: String,
    val orgUserId: String,
    val orgUserName: String,
    val itemId: String,
    val itemName: String,
    val isActive: Boolean,
    val orgId: String,
    val categoryId: String,
    val categoryName: String,
    val batchId: String,
    val batchNumber: String,
    val receivedDate: String,
    val expirationDate: String,
    val purchasePrice: Double,
    val billingPrice: Double,
    val quantity: Int,
    val syncStatus: SyncStatus,
    )
