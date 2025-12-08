package com.datavite.distrivite.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class RemoteStudent(
    @SerialName("id") val id: String,
    @SerialName("created") val created: String,
    @SerialName("modified") val modified: String,
    @SerialName("org_id") val orgId: String,
    @SerialName("user_id") val userId: String,
    @SerialName("has_mailing_tracking") val hasMailingTracking:Boolean,
    @SerialName("has_sms_tracking") val hasSmsTracking:Boolean,
    @SerialName("is_delegate") val isDelegate:Boolean,
    @SerialName("is_active") val isActive:Boolean,
    @SerialName("education_class_id") val educationClassId: String,
    @SerialName("education_class") val educationClass: String,
    @SerialName("org_slug") val orgSlug: String,
    @SerialName("name") val name: String,
    @SerialName("embeddings") val embeddings: List<List<Float>>,
)
