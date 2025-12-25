package com.datavite.distrivite.data.remote.datasource

import com.datavite.distrivite.data.remote.model.RemoteBulkCreditPayment

interface BulkCreditPaymentRemoteDataSource {
    suspend fun getRemoteBulkCreditPaymentIds(organization: String): List<String>
    suspend fun getRemoteBulkCreditPaymentChangesSince(organization: String, since: Long): List<RemoteBulkCreditPayment>
    suspend fun getRemoteBulkCreditPayments(organization: String): List<RemoteBulkCreditPayment>
    suspend fun createRemoteBulkCreditPayment(organization: String, remoteBulkCreditPayment: RemoteBulkCreditPayment): RemoteBulkCreditPayment
    suspend fun updateRemoteBulkCreditPayment(organization: String, remoteBulkCreditPayment: RemoteBulkCreditPayment): RemoteBulkCreditPayment
    suspend fun deleteRemoteBulkCreditPayment(organization: String, bulkCreditPaymentId: String): RemoteBulkCreditPayment
}