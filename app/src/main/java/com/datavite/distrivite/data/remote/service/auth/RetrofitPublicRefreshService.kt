package com.datavite.distrivite.data.remote.service.auth

import com.datavite.distrivite.data.remote.model.auth.AuthRefreshRequest
import com.datavite.distrivite.data.remote.model.auth.AuthRefreshResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface RetrofitPublicRefreshService {
    @POST("en/api/v1/auth/jwt/refresh/")
    suspend fun refresh(
        @Body request: AuthRefreshRequest
    ): AuthRefreshResponse
}