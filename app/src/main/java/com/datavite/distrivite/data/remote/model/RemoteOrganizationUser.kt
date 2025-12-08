package com.datavite.distrivite.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteOrganizationUser(
    @SerialName("id") val id: String,
    @SerialName("created") val created: String,
    @SerialName("modified") val modified: String,
    @SerialName("org_id") val orgId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("is_admin") val isAdmin: Boolean,
    @SerialName("is_active") val isActive: Boolean,
    @SerialName("org_slug") val orgSlug: String,
    @SerialName("name") val name: String,
    val embeddings: List<List<Float>>,
)
