package com.datavite.distrivite.data.remote.model
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class RemoteWorkingPeriod(
    @SerialName("id")
    val id: String,

    @SerialName("created")
    val created: String,

    @SerialName("modified")
    val modified: String,

    @SerialName("day_id")
    val dayId: Int,

    @SerialName("org_id")
    val orgId: String,

    @SerialName("org_slug")
    val orgSlug: String,

    @SerialName("is_active")
    val isActive: Boolean,

    @SerialName("day")
    val day: String,

    @SerialName("start_time")
    val startTime: String,

    @SerialName("end_time")
    val endTime: String,
)

