package com.datavite.distrivite.domain.model

import androidx.room.PrimaryKey
import com.datavite.distrivite.data.local.model.SyncStatus
import com.datavite.distrivite.utils.TransactionBroker
import com.datavite.distrivite.utils.TransactionType

data class DomainTransaction(
    @PrimaryKey val id: String,
    val created: String,
    val modified: String,
    val orgSlug: String,
    val orgId: String,
    val orgUserId: String,
    val orgUserName: String,
    val participant: String,
    val reason: String,
    val amount: Double,
    val transactionType: TransactionType,
    val transactionBroker: TransactionBroker,
    val syncStatus: SyncStatus
)
