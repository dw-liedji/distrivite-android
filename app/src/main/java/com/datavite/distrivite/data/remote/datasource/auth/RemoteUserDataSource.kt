package com.datavite.distrivite.data.remote.datasource.auth

import com.datavite.distrivite.data.remote.clients.Response
import com.datavite.distrivite.data.remote.model.auth.AuthOrgUser
import com.datavite.distrivite.data.remote.model.auth.AuthOrgUserRequest
import com.datavite.distrivite.data.remote.model.auth.AuthUser

interface RemoteUserDataSource  {
    suspend fun getUserProfile():Response<AuthUser>
    suspend fun authOrgUser(request:AuthOrgUserRequest): Response<AuthOrgUser>
}