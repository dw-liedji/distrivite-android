package com.datavite.distrivite.data.local.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.datavite.distrivite.utils.TransactionBroker

@Entity(
    tableName = "localBillingPayments",
    foreignKeys = [ForeignKey(
        entity = LocalBilling::class,
        parentColumns = ["id"],
        childColumns = ["billingId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("billingId")]
)
data class LocalBillingPayment(
    @PrimaryKey val id: String,
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

