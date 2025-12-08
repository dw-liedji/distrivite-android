package com.datavite.distrivite.data.remote.model.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthOrg(
    @SerialName("id") val id:String,
    @SerialName("created") val created: String,
    @SerialName("name") val name:String,
    @SerialName("slug") val slug:String,
    @SerialName("latitude") val latitude:String,
    @SerialName("longitude") val longitude:String,
    @SerialName("login_credential") val loginCredential:String,
    @SerialName("password_credential") val passwordCredential:String,
)
