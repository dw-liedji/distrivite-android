package com.datavite.distrivite.data.remote.service

import com.datavite.distrivite.data.remote.model.RemoteTransaction
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface RemoteTransactionService {
    @GET("en/{organization}/api/v1/data/transactions/")
    suspend fun getRemoteTransactions(
        @Path("organization") organization: String, // Assuming 'modified' is a date field, adjust type accordingly
     ): List<RemoteTransaction>

    @GET("en/{organization}/api/v1/data/transaction-ids/")
    suspend fun getRemoteTransactionIds(
        @Path("organization") organization: String, // Assuming 'modified' is a date field, adjust type accordingly
    ): List<String>

    @GET("en/{organization}/api/v1/data/transaction-changes/")
    suspend fun getRemoteTransactionsChangesSince(
        @Path("organization") organization: String,
        @Query("since") since: Long  // Change from String to Long
    ): List<RemoteTransaction>

    @POST("en/{organization}/api/v1/data/transactions/create/")
    suspend fun createRemoteTransaction(
        @Path("organization") organization: String,
        @Body remoteRemoteTransaction: RemoteTransaction
    ): RemoteTransaction

    @PUT("en/{organization}/api/v1/data/transactions/{id}/edit/")
    suspend fun updateRemoteTransaction(
        @Path("organization") organization: String,
        @Path("id") id: String,
        @Body remoteTransaction: RemoteTransaction
    ): RemoteTransaction

    @DELETE("en/{organization}/api/v1/data/transactions/{id}/delete/")
    suspend fun deleteRemoteTransaction(
        @Path("organization") organization: String,
        @Path("id") id: String
    ): RemoteTransaction

}
