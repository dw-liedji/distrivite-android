package com.datavite.distrivite.data.remote.model.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrgUserAuthRequest(
    @SerialName("credential") val credential:String,
    @SerialName("user_id") val userId:String,
)
