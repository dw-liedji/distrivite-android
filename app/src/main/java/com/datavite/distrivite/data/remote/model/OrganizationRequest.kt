package com.datavite.distrivite.data.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserOrganizationSignInRequest(
    @SerialName("email_credential") val emailCredential:String,
    @SerialName("organization_credential") val organizationCredential:String,
    @SerialName("password_credential") val passwordCredential:String,
)
