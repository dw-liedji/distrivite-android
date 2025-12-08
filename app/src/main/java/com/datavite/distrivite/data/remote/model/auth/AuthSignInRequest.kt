package com.datavite.distrivite.data.remote.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthSignInRequest (
    val email:String,
    val password:String
)