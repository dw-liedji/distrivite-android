package com.datavite.distrivite.data.remote.service

import com.datavite.distrivite.data.remote.model.RemoteTeachingPeriod
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path


interface TeachingPeriodService {
    @GET("en/{organization}/api/v2/teaching-periods/")
    suspend fun getTeachingPeriods(
        @Path("organization") organization: String, // Assuming 'modified' is a date field, adjust type accordingly
     ): List<RemoteTeachingPeriod>

    @POST("en/{organization}/api/v2/teaching-periods/")
    suspend fun createTeachingPeriod(
        @Path("organization") organization: String,
        @Body remoteTeachingPeriod: RemoteTeachingPeriod
    ): RemoteTeachingPeriod

    @POST("en/{organization}/api/v2/teaching-periods/{id}/edit")
    suspend fun updateTeachingPeriod(
        @Path("organization") organization: String,
        @Path("id") id: String,
        @Body period: RemoteTeachingPeriod
    ): RemoteTeachingPeriod

    @DELETE("en/{organization}/api/v2/teaching-periods/{id}")
    suspend fun deleteTeachingPeriod(
        @Path("organization") organization: String,
        @Path("id") id: String
    )
}
