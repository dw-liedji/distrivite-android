package com.datavite.distrivite.domain.model

import com.datavite.distrivite.R
import com.datavite.distrivite.data.local.model.SyncStatus

data class DomainStock(
    val id: String,
    val itemId: String,
    val created: String,
    val modified: String,
    val orgSlug: String,
    val itemName: String,
    val isActive: Boolean,
    val orgId: String,
    val orgUserId: String,
    val orgUserName: String,
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
    val imageUrl: Int = R.drawable.no_org,
)
