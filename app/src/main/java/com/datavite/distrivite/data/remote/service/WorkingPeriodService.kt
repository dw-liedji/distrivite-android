package com.datavite.distrivite.data.remote.service

import com.datavite.distrivite.data.remote.model.RemoteWorkingPeriod
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface WorkingPeriodService {
    @GET("en/{organization}/api/v2/working-periods/")
    suspend fun getWorkingPeriods(
        @Path("organization") organization: String, // Assuming 'modified' is a date field, adjust type accordingly
     ): List<RemoteWorkingPeriod>


    @POST("en/{organization}/api/v2/working-periods/")
    suspend fun createWorkingPeriod(
        @Path("organization") organization: String,
        @Body domainWorkingPeriod: RemoteWorkingPeriod
    ): RemoteWorkingPeriod

    @DELETE("en/{organization}/api/v2/working-periods/{id}")
    suspend fun deleteWorkingPeriod(
        @Path("organization") organization: String,
        @Path("id") id: Long
    )
}
