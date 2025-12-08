package com.datavite.distrivite.data.remote.datasource

import com.datavite.distrivite.data.remote.model.RemoteTransaction
import com.datavite.distrivite.data.remote.service.RemoteTransactionService
import javax.inject.Inject

class TransactionRemoteDataSourceImpl @Inject constructor(
    private val remoteTransactionService: RemoteTransactionService
) : TransactionRemoteDataSource {
    override suspend fun getRemoteTransactionIds(organization: String): List<String> {
        return remoteTransactionService.getRemoteTransactionIds(organization)
    }

    override suspend fun getRemoteTransactionsChangesSince(
        organization: String,
        since: Long
    ): List<RemoteTransaction> {
        return remoteTransactionService.getRemoteTransactionsChangesSince(organization, since)
    }

    override suspend fun getRemoteTransactions(organization: String): List<RemoteTransaction> {
        return remoteTransactionService.getRemoteTransactions(organization)
    }

    override suspend fun createRemoteTransaction(organization: String, remoteTransaction: RemoteTransaction): RemoteTransaction {
        return remoteTransactionService.createRemoteTransaction(organization, remoteTransaction)
    }

    override suspend fun updateRemoteTransaction(organization: String, remoteTransaction: RemoteTransaction): RemoteTransaction {
        return remoteTransactionService.updateRemoteTransaction(organization, remoteTransaction.id, remoteTransaction)
    }

    override suspend fun deleteRemoteTransaction(organization: String, transactionId: String): RemoteTransaction {
        return remoteTransactionService.deleteRemoteTransaction(organization, transactionId)
    }
}