package com.datavite.distrivite.domain.model

import com.datavite.distrivite.data.local.model.SyncStatus
import com.datavite.distrivite.utils.TransactionBroker

data class DomainBulkCreditPayment(
    val id: String,
    val created: String,
    val modified: String,
    val customerId: String,
    val billNumber:String,
    val customerName:String,
    val orgId: String,
    val orgSlug: String,
    val orgUserId: String,
    val orgUserName: String,
    val transactionBroker: TransactionBroker,
    val amount: Double,
    val syncStatus: SyncStatus
)