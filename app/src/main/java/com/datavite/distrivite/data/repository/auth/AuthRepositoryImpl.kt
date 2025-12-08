package com.datavite.distrivite.data.repository.auth

import android.util.Log
import com.datavite.distrivite.data.remote.clients.Response
import com.datavite.distrivite.data.remote.datasource.auth.RemoteAuthDataSource
import com.datavite.distrivite.data.remote.model.auth.AuthSignInResponse
import com.datavite.distrivite.data.remote.model.auth.AuthSignUpRequest
import com.datavite.distrivite.data.remote.model.auth.AuthSignUpResponse
import com.datavite.distrivite.domain.repository.auth.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor (
    private val remoteAuthDataSource: RemoteAuthDataSource
): AuthRepository {

    override suspend fun signUp(authSignUpRequest: AuthSignUpRequest): Response<AuthSignUpResponse> {
        Log.i("Retrofit:", "repository")
       return remoteAuthDataSource.signUp(authSignUpRequest=authSignUpRequest)
    }
    override suspend fun signIn(email: String, password: String): Response<AuthSignInResponse> {
        return remoteAuthDataSource.signIn(email=email, password=password)
    }

}