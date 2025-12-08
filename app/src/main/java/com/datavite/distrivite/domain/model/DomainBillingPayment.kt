package com.datavite.distrivite.domain.model

import com.datavite.distrivite.data.local.model.SyncStatus
import com.datavite.distrivite.utils.TransactionBroker

data class DomainBillingPayment(
    val id: String,
    val created: String,
    val modified: String,
    val orgSlug: String,
    val orgId: String,
    val orgUserId: String,
    val billingId: String,
    val transactionBroker: TransactionBroker,
    val amount: Double,
    val syncStatus: SyncStatus
)