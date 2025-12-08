package com.datavite.distrivite.data.repository.auth

import com.datavite.distrivite.data.remote.datasource.auth.RemoteUserDataSource
import com.datavite.distrivite.data.remote.model.auth.AuthOrgUser
import com.datavite.distrivite.data.remote.model.auth.AuthOrgUserRequest
import com.datavite.distrivite.data.remote.model.auth.AuthUser
import com.datavite.distrivite.domain.repository.auth.UserRepository
import com.datavite.distrivite.data.remote.clients.Response
import javax.inject.Inject

class UserRepositoryImpl @Inject constructor (private val remoteUserDataSource: RemoteUserDataSource) : UserRepository {
    override suspend fun getUserProfile(): Response<AuthUser> {
        return remoteUserDataSource.getUserProfile()
    }

    override suspend fun authOrgUser(request: AuthOrgUserRequest): Response<AuthOrgUser> {
        return remoteUserDataSource.authOrgUser(request)
    }
}