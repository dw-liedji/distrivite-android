package com.datavite.distrivite.data.local.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.datavite.distrivite.utils.TransactionBroker
import kotlinx.serialization.SerialName

@Entity(
    tableName = "localBulkCreditPayments",
    foreignKeys = [
        ForeignKey(
            entity = LocalCustomer::class, // Assuming you have a LocalCustomer entity
            parentColumns = ["id"],
            childColumns = ["customerId"],
            onDelete = ForeignKey.CASCADE
        ),
    ],
    indices = [
        Index("customerId"),
    ]
)
data class LocalBulkCreditPayment(
    @PrimaryKey val id: String,
    val created: String, // Assuming this comes as ISO string from server
    val modified: String, // Assuming this comes as ISO string from server
    val customerId: String,
    val billNumber:String,
    val customerName:String,
    val orgSlug: String,
    val orgId: String, // Changed from "organization" ID
    val orgUserId: String,
    val orgUserName: String,
    val transactionBroker: TransactionBroker,
    val amount: Double,
    val syncStatus: SyncStatus = SyncStatus.PENDING,
)