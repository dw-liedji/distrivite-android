package com.datavite.distrivite.data.remote.model
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteBillingItem(
    @SerialName("id") val id: String,
    @SerialName("created")  val created: String,
    @SerialName("modified")  val modified: String,
    @SerialName("organization_slug") val orgSlug: String,
    @SerialName("organization_id") val orgId: String,
    @SerialName("organization_user_id") val orgUserId: String,
    @SerialName("stock_name") val stockName: String,
    @SerialName("stock_id") val stockId: String,
    @SerialName("is_delivered") val isDelivered: Boolean,
    @SerialName("facturation_id") val billingId: String,
    @SerialName("quantity") val quantity: Int,
    @SerialName("unit_price") val unitPrice: Double,
)