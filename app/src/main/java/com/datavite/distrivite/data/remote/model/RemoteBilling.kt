package com.datavite.distrivite.data.remote.model
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteBilling(
    @SerialName("id")
    val id: String,
    @SerialName("created")
    val created: String,
    @SerialName("modified")
    val modified: String,
    @SerialName("organization_slug")
    val orgSlug: String,
    @SerialName("organization_id")
    val orgId: String,
    @SerialName("organization_user_id")
    val orgUserId: String,
    @SerialName("organization_user_name")
    val orgUserName: String,
    @SerialName("bill_number")
    val billNumber: String,
    @SerialName("placed_at")
    val placedAt: String,
    @SerialName("customer_id")
    val customerId: String,
    @SerialName("customer_name")
    val customerName: String,
    @SerialName("customer_phone_number")
    val customerPhoneNumber: String?,
    @SerialName("is_delivered") val isDelivered: Boolean,
    @SerialName("facturation_stocks") val items: List<RemoteBillingItem>,
    @SerialName("facturation_payments") val payments: List<RemoteBillingPayment>
)

