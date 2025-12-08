package com.datavite.distrivite.data.remote.service

import com.datavite.distrivite.data.remote.model.RemoteLeave
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


interface LeaveService {
    @GET("en/{organization}/api/v2/leaves/")
    suspend fun getLeaves(
        @Path("organization") organization: String,
     ): List<RemoteLeave>

    @POST("en/{organization}/api/v2/leaves/create")
    suspend fun createLeave(
        @Path("organization") organization: String,
        @Body remoteLeave: RemoteLeave
    ): RemoteLeave

    @POST("en/{organization}/api/v2/leaves/{id}/edit")
    suspend fun updateLeave(
            @Path("organization") organization: String,
            @Path("id") id: String,
            @Body remoteLeave: RemoteLeave
    ): RemoteLeave

    @DELETE("en/{organization}/api/v2/leaves/{id}/delete")
    suspend fun deleteLeave(
        @Path("organization") organization: String,
        @Path("id") id: String,
        @Body remoteLeave: RemoteLeave
    ): RemoteLeave
}
