package com.datavite.distrivite.data.remote.model
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteCustomer(
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
    @SerialName("name")
    val name: String,
    @SerialName("phone_number")
    val phoneNumber: String?,
)

