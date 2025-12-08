package com.datavite.distrivite.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class RemoteRoom(
    @SerialName("id") val id:String,
    @SerialName("created") val created: String,
    @SerialName("modified") val modified: String,
    @SerialName("org_id") val orgId: String,
    @SerialName("org_slug") val orgSlug: String,
    @SerialName("name") val name:String,
)