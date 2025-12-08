package com.datavite.distrivite.data.remote.datasource

import com.datavite.distrivite.data.remote.model.RemoteTransaction

interface TransactionRemoteDataSource {
    suspend fun getRemoteTransactionIds(organization: String): List<String>
    suspend fun getRemoteTransactionsChangesSince(organization: String, since: Long): List<RemoteTransaction>
    suspend fun getRemoteTransactions(organization: String): List<RemoteTransaction>
    suspend fun createRemoteTransaction(organization: String, remoteTransaction: RemoteTransaction): RemoteTransaction
    suspend fun updateRemoteTransaction(organization: String, remoteTransaction: RemoteTransaction): RemoteTransaction
    suspend fun deleteRemoteTransaction(organization: String, transactionId: String): RemoteTransaction
}