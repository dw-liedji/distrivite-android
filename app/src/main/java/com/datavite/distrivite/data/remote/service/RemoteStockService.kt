package com.datavite.distrivite.data.remote.service

import com.datavite.distrivite.data.remote.model.RemoteStock
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface RemoteStockService {
    @GET("en/{organization}/api/v1/data/stocks/")
    suspend fun getRemoteStocks(
        @Path("organization") organization: String, // Assuming 'modified' is a date field, adjust type accordingly
     ): List<RemoteStock>

    @GET("en/{organization}/api/v1/data/stock-ids/")
    suspend fun getRemoteStockIds(
        @Path("organization") organization: String, // Assuming 'modified' is a date field, adjust type accordingly
    ): List<String>

    @GET("en/{organization}/api/v1/data/stock-changes/")
    suspend fun getRemoteStocksChangesSince(
        @Path("organization") organization: String,
        @Query("since") since: Long  // Change from String to Long
    ): List<RemoteStock>

    @POST("en/{organization}/api/v1/data/stocks/create/")
    suspend fun createRemoteStock(
        @Path("organization") organization: String,
        @Body remoteRemoteStock: RemoteStock
    ): RemoteStock

    @PUT("en/{organization}/api/v1/data/stocks/{id}/edit/")
    suspend fun updateRemoteStock(
        @Path("organization") organization: String,
        @Path("id") id: String,
        @Body session: RemoteStock
    ): RemoteStock

    @PUT("en/{organization}/api/v1/data/stocks/{id}/edit-quantity/")
    suspend fun updateRemoteStockQuantity(
        @Path("organization") organization: String,
        @Path("id") id: String,
        @Body session: RemoteStock
    ): RemoteStock


    @DELETE("en/{organization}/api/v1/data/stocks/{id}/delete/")
    suspend fun deleteRemoteStock(
        @Path("organization") organization: String,
        @Path("id") id: String
    ): RemoteStock

}
