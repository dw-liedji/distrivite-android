package com.datavite.distrivite.data.remote.model.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthOrgUser(
    @SerialName("id") val id: String,
    @SerialName("created") val created: String,
    @SerialName("modified") val modified: String,
    @SerialName("org_id") val orgId: String,
    @SerialName("is_active") val isActive: Boolean,
    @SerialName("is_admin") val isAdmin: Boolean,
    @SerialName("is_manager") val isManager: Boolean,
    @SerialName("is_device") val isDevice: Boolean,
    @SerialName("is_gps_active") val isGPSActive: Boolean,
    @SerialName("is_liveness_active") val isLivenessActive: Boolean,
    @SerialName("check_in_latitude") val checkInLatitude:Double,
    @SerialName("check_in_longitude") val checkInLongitude:Double,
    @SerialName("check_out_latitude") val checkOutLatitude:Double,
    @SerialName("check_out_longitude") val checkOutLongitude:Double,
    @SerialName("radius") val radius:Double,
    @SerialName("user_id") val userId: String,
    @SerialName("org_credential") val orgCredential: String,
    @SerialName("org_slug") val orgSlug: String,
    @SerialName("name") val name: String,
    @SerialName("embeddings") val embeddings: List<List<Float>>,
    @SerialName("permissions") val permissions: Set<String>,
)


