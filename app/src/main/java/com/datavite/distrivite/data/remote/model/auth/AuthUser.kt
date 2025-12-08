package com.datavite.distrivite.data.remote.model.auth

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AuthUser(
    @SerialName("id")
    val id:String,
    @SerialName("email")
    val email:String,
    @SerialName("username")
    val username:String
)
