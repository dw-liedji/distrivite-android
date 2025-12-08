package com.datavite.distrivite.data.remote.service

import com.datavite.distrivite.data.remote.model.RemoteCustomer
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface RemoteCustomerService {
    @GET("en/{organization}/api/v1/data/customers/")
    suspend fun getRemoteCustomers(
        @Path("organization") organization: String, // Assuming 'modified' is a date field, adjust type accordingly
     ): List<RemoteCustomer>

    @GET("en/{organization}/api/v1/data/customer-ids/")
    suspend fun getRemoteCustomerIds(
        @Path("organization") organization: String, // Assuming 'modified' is a date field, adjust type accordingly
    ): List<String>

    @GET("en/{organization}/api/v1/data/customer-changes/")
    suspend fun getRemoteCustomersChangesSince(
        @Path("organization") organization: String,
        @Query("since") since: Long  // Change from String to Long
    ): List<RemoteCustomer>

    @POST("en/{organization}/api/v1/data/customers/create/")
    suspend fun createRemoteCustomer(
        @Path("organization") organization: String,
        @Body remoteRemoteCustomer: RemoteCustomer
    ): RemoteCustomer

    @PUT("en/{organization}/api/v1/data/customers/{id}/edit/")
    suspend fun updateRemoteCustomer(
        @Path("organization") organization: String,
        @Path("id") id: String,
        @Body session: RemoteCustomer
    ): RemoteCustomer

    @DELETE("en/{organization}/api/v1/data/customers/{id}/delete/")
    suspend fun deleteRemoteCustomer(
        @Path("organization") organization: String,
        @Path("id") id: String
    ): RemoteCustomer

}
