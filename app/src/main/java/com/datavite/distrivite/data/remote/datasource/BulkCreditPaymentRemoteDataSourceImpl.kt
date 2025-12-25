package com.datavite.distrivite.data.remote.datasource

import com.datavite.distrivite.data.remote.model.RemoteBulkCreditPayment
import com.datavite.distrivite.data.remote.service.RemoteBulkCreditPaymentService
import javax.inject.Inject

class BulkCreditPaymentRemoteDataSourceImpl @Inject constructor(
    private val remoteBulkCreditPaymentService: RemoteBulkCreditPaymentService
) : BulkCreditPaymentRemoteDataSource {

    override suspend fun getRemoteBulkCreditPaymentIds(organization: String): List<String> {
        return remoteBulkCreditPaymentService.getRemoteBulkCreditPaymentIds(organization)
    }

    override suspend fun getRemoteBulkCreditPaymentChangesSince(
        organization: String,
        since: Long
    ): List<RemoteBulkCreditPayment> {
        return remoteBulkCreditPaymentService.getRemoteBulkCreditPaymentChangesSince(organization, since)
    }

    override suspend fun getRemoteBulkCreditPayments(organization: String): List<RemoteBulkCreditPayment> {
        return remoteBulkCreditPaymentService.getRemoteBulkCreditPayments(organization)
    }

    override suspend fun createRemoteBulkCreditPayment(
        organization: String,
        remoteBulkCreditPayment: RemoteBulkCreditPayment
    ): RemoteBulkCreditPayment {
        return remoteBulkCreditPaymentService.createRemoteBulkCreditPayment(organization, remoteBulkCreditPayment)
    }

    override suspend fun updateRemoteBulkCreditPayment(
        organization: String,
        remoteBulkCreditPayment: RemoteBulkCreditPayment
    ): RemoteBulkCreditPayment {
        return remoteBulkCreditPaymentService.updateRemoteBulkCreditPayment(
            organization,
            remoteBulkCreditPayment.id,
            remoteBulkCreditPayment
        )
    }

    override suspend fun deleteRemoteBulkCreditPayment(
        organization: String,
        bulkCreditPaymentId: String
    ): RemoteBulkCreditPayment {
        return remoteBulkCreditPaymentService.deleteRemoteBulkCreditPayment(organization, bulkCreditPaymentId)
    }
}