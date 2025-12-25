package com.datavite.distrivite.data.remote.model

import com.datavite.distrivite.utils.TransactionBroker
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteBulkCreditPayment(
    @SerialName("id") val id: String,
    @SerialName("created") val created: String,
    @SerialName("modified") val modified: String,
    @SerialName("customer_id") val customerId: String, // Changed from "customer" ID
    @SerialName("organization_slug") val orgSlug: String,
    @SerialName("organization_id") val orgId: String, // Changed from "organization" ID
    @SerialName("organization_user_id") val orgUserId: String,
    @SerialName("bill_number") val billNumber:String,
    @SerialName("customer_name") val customerName:String,
    @SerialName("organization_user_name") val orgUserName: String,
    @SerialName("transaction_broker") val transactionBroker: TransactionBroker,
    @SerialName("amount") val amount: Double
)