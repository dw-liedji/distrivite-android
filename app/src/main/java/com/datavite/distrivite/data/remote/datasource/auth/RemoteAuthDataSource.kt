package com.datavite.distrivite.data.remote.datasource.auth

import com.datavite.distrivite.data.remote.clients.Response
import com.datavite.distrivite.data.remote.model.auth.AuthSignInResponse
import com.datavite.distrivite.data.remote.model.auth.AuthSignUpRequest
import com.datavite.distrivite.data.remote.model.auth.AuthSignUpResponse

interface RemoteAuthDataSource {
    suspend fun signUp(authSignUpRequest: AuthSignUpRequest): Response<AuthSignUpResponse>
    suspend fun signIn(email:String, password:String): Response<AuthSignInResponse>
}