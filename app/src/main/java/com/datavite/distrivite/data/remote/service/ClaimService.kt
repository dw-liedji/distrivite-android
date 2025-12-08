package com.datavite.distrivite.data.remote.service

import com.datavite.distrivite.data.remote.model.RemoteClaim
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


interface ClaimService {
    @GET("en/{organization}/api/v2/claims/")
    suspend fun getClaims(
        @Path("organization") organization: String,
     ): List<RemoteClaim>

    @POST("en/{organization}/api/v2/claims/create")
    suspend fun createClaim(
        @Path("organization") organization: String,
        @Body remoteClaim: RemoteClaim
    ): RemoteClaim

    @POST("en/{organization}/api/v2/claims/{id}/edit")
    suspend fun updateClaim(
            @Path("organization") organization: String,
            @Path("id") id: String,
            @Body remoteClaim: RemoteClaim
    ): RemoteClaim

    @DELETE("en/{organization}/api/v2/claims/{id}/delete")
    suspend fun deleteClaim(
        @Path("organization") organization: String,
        @Path("id") id: String,
        @Body remoteClaim: RemoteClaim
    ): RemoteClaim
}
