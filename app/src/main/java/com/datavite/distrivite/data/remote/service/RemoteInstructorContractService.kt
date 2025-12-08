package com.datavite.distrivite.data.remote.service

import com.datavite.distrivite.data.remote.model.RemoteInstructorContract
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path


interface RemoteInstructorContractService {
    @GET("en/{organization}/api/v2/instructors/")
    suspend fun getRemoteInstructorContracts(
        @Path("organization") organization: String,
     ): List<RemoteInstructorContract>

    @POST("en/{organization}/api/v2/instructors/create")
    suspend fun createRemoteInstructorContract(
        @Path("organization") organization: String,
        @Body remoteInstructorContract: RemoteInstructorContract
    ): RemoteInstructorContract

    @PUT("en/{organization}/api/v2/instructors/{id}/edit")
    suspend fun updateRemoteInstructorContract(
            @Path("organization") organization: String,
            @Path("id") id: String,
            @Body remoteInstructorContract: RemoteInstructorContract
    ): RemoteInstructorContract

    @DELETE("en/{organization}/api/v2/instructors/{id}/delete")
    suspend fun deleteRemoteInstructorContract(
        @Path("organization") organization: String,
        @Path("id") id: String,
        @Body remoteInstructorContract: RemoteInstructorContract
    ): RemoteInstructorContract
}
