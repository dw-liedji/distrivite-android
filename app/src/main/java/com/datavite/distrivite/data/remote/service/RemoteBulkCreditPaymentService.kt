package com.datavite.distrivite.data.remote.service

import com.datavite.distrivite.data.remote.model.RemoteBulkCreditPayment
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface RemoteBulkCreditPaymentService {
    @GET("en/{organization}/api/v1/data/bulk-credit-payments/")
    suspend fun getRemoteBulkCreditPayments(
        @Path("organization") organization: String
    ): List<RemoteBulkCreditPayment>

    @GET("en/{organization}/api/v1/data/bulk-credit-payment-ids/")
    suspend fun getRemoteBulkCreditPaymentIds(
        @Path("organization") organization: String
    ): List<String>

    @GET("en/{organization}/api/v1/data/bulk-credit-payment-changes/")
    suspend fun getRemoteBulkCreditPaymentChangesSince(
        @Path("organization") organization: String,
        @Query("since") since: Long
    ): List<RemoteBulkCreditPayment>

    @POST("en/{organization}/api/v1/data/bulk-credit-payments/create/")
    suspend fun createRemoteBulkCreditPayment(
        @Path("organization") organization: String,
        @Body remoteBulkCreditPayment: RemoteBulkCreditPayment
    ): RemoteBulkCreditPayment

    @PUT("en/{organization}/api/v1/data/bulk-credit-payments/{id}/edit/")
    suspend fun updateRemoteBulkCreditPayment(
        @Path("organization") organization: String,
        @Path("id") id: String,
        @Body remoteBulkCreditPayment: RemoteBulkCreditPayment
    ): RemoteBulkCreditPayment

    @DELETE("en/{organization}/api/v1/data/bulk-credit-payments/{id}/delete/")
    suspend fun deleteRemoteBulkCreditPayment(
        @Path("organization") organization: String,
        @Path("id") id: String
    ): RemoteBulkCreditPayment
}