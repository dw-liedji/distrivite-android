package com.datavite.distrivite.domain.model

import com.datavite.distrivite.data.local.model.SyncStatus

data class DomainBillingItem(
    val id: String,
    val created: String,
    val modified: String,
    val orgSlug: String,
    val orgId: String,
    val orgUserId: String,
    val billingId: String,
    val stockId: String,
    val stockName: String,
    val quantity: Int,
    val unitPrice: Double,
    val syncStatus: SyncStatus
)