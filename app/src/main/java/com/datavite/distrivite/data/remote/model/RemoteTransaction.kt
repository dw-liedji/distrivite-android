package com.datavite.distrivite.data.remote.model

import androidx.room.PrimaryKey
import com.datavite.distrivite.utils.TransactionBroker
import com.datavite.distrivite.utils.TransactionType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteTransaction(
    @PrimaryKey val id: String,
    @SerialName("created") val created: String,
    @SerialName("modified") val modified: String,
    @SerialName("organization_slug") val orgSlug: String,
    @SerialName("organization_id") val orgId: String,
    @SerialName("organization_user_id") val orgUserId: String,
    @SerialName("organization_user_name") val orgUserName: String,
    @SerialName("participant")  val participant: String,
    @SerialName("reason") val reason: String,
    @SerialName("amount") val amount: Double,
    @SerialName("transaction_type")val transactionType: TransactionType,
    @SerialName("transaction_broker") val transactionBroker: TransactionBroker,
)
