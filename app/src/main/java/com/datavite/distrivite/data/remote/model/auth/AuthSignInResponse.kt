package com.datavite.distrivite.data.remote.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthSignInResponse (
    val refresh:String,
    val access:String,
)