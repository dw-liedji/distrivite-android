package com.datavite.distrivite.data.remote.model.auth

import kotlinx.serialization.Serializable

@Serializable
data class AuthRefreshResponse (
    val access:String,
)