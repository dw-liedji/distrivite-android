package com.datavite.distrivite.data.remote.model
import com.datavite.distrivite.utils.TransactionBroker
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteBillingPayment(
    @SerialName("id") val id: String,
    @SerialName("created")  val created: String,
    @SerialName("modified")  val modified: String,
    @SerialName("organization_slug") val orgSlug: String,
    @SerialName("organization_id") val orgId: String,
    @SerialName("organization_user_id") val orgUserId: String,
    @SerialName("facturation_id") val billingId: String,
    @SerialName("transaction_broker") val transactionBroker: TransactionBroker,
    @SerialName("amount") val amount: Double,
)
