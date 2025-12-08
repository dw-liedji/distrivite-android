package com.datavite.distrivite.data.remote.service

import com.datavite.distrivite.data.remote.model.RemoteEmployee
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path


interface EmployeeService {
    @GET("en/{organization}/api/v2/employees/")
    suspend fun getEmployees(
        @Path("organization") organization: String,
     ): List<RemoteEmployee>

    @POST("en/{organization}/api/v2/employees/create")
    suspend fun createUser(
        @Path("organization") organization: String,
        @Body remoteEmployee: RemoteEmployee
    ): RemoteEmployee

    @PUT("en/{organization}/api/v2/employees/{id}/edit")
    suspend fun updateEmployee(
            @Path("organization") organization: String,
            @Path("id") id: String,
            @Body remoteEmployee: RemoteEmployee
    ): RemoteEmployee

    @DELETE("en/{organization}/api/v2/employees/{id}/delete")
    suspend fun deleteEmployee(
        @Path("organization") organization: String,
        @Path("id") id: String,
        @Body remoteEmployee: RemoteEmployee
    ): RemoteEmployee
}
