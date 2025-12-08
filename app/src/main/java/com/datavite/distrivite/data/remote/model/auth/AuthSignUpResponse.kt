package com.datavite.distrivite.data.remote.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthSignUpResponse(
    val id:String,
    val first_name:String,
    val last_name:String,
    val email:String,
    val birth_date:String,
    val password:String
)
