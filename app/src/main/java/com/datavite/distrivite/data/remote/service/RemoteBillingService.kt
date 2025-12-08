package com.datavite.distrivite.data.remote.service

import com.datavite.distrivite.data.remote.model.RemoteBilling
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface RemoteBillingService {

    @GET("en/{organization}/api/v1/data/sales/")
    suspend fun getRemoteBillings(
        @Path("organization") organization: String, // Assuming 'modified' is a date field, adjust type accordingly
     ): List<RemoteBilling>

    @GET("en/{organization}/api/v1/data/sale-ids/")
    suspend fun getRemoteBillingIds(
        @Path("organization") organization: String, // Assuming 'modified' is a date field, adjust type accordingly
    ): List<String>

    @GET("en/{organization}/api/v1/data/sale-changes/")
    suspend fun getRemoteBillingsChangesSince(
        @Path("organization") organization: String,
        @Query("since") since: Long  // Change from String to Long
    ): List<RemoteBilling>


    @POST("en/{organization}/api/v1/data/sales/create/")
    suspend fun createRemoteBilling(
        @Path("organization") organization: String,
        @Body remoteRemoteBilling: RemoteBilling
    ): RemoteBilling

    @PUT("en/{organization}/api/v1/data/sales/{id}/edit/")
    suspend fun updateRemoteBilling(
        @Path("organization") organization: String,
        @Path("id") id: String,
        @Body session: RemoteBilling
    ): RemoteBilling

    @PUT("en/{organization}/api/v1/data/sales/{id}/deliver/")
    suspend fun deleverRemoteBilling(
        @Path("organization") organization: String,
        @Path("id") id: String,
        @Body session: RemoteBilling
    ): RemoteBilling

    @DELETE("en/{organization}/api/v1/data/sales/{id}/delete/")
    suspend fun deleteRemoteBilling(
        @Path("organization") organization: String,
        @Path("id") id: String
    ): RemoteBilling
}
