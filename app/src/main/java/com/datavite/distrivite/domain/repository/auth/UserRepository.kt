package com.datavite.distrivite.domain.repository.auth

import com.datavite.distrivite.data.remote.model.auth.AuthOrgUser
import com.datavite.distrivite.data.remote.model.auth.AuthOrgUserRequest
import com.datavite.distrivite.data.remote.model.auth.AuthUser
import com.datavite.distrivite.data.remote.clients.Response

interface UserRepository {
    suspend fun getUserProfile(): Response<AuthUser>
    suspend fun authOrgUser(request: AuthOrgUserRequest): Response<AuthOrgUser>

}