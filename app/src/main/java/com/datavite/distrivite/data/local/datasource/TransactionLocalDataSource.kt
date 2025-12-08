package com.datavite.distrivite.data.local.datasource

import FilterOption
import com.datavite.distrivite.data.local.model.LocalTransaction
import com.datavite.distrivite.data.local.model.SyncStatus
import com.datavite.distrivite.utils.TransactionBroker
import com.datavite.distrivite.utils.TransactionType
import kotlinx.coroutines.flow.Flow

interface TransactionLocalDataSource {
    suspend fun getLocalTransactionIds(): List<String>
    suspend fun getLocalTransactionsFlow(): Flow<List<LocalTransaction>>
    suspend fun getLocalTransactionsFor(searchQuery: String, filterOption: FilterOption): List<LocalTransaction>
    suspend fun getLocalTransactionsForFilterOption(filterOption: FilterOption): List<LocalTransaction>
    suspend fun getLocalTransactionById(transactionId: String): LocalTransaction?
    suspend fun insertLocalTransaction(localTransaction: LocalTransaction)
    suspend fun saveLocalTransactions(localTransactions: List<LocalTransaction>)
    suspend fun updateSyncStatus(id: String, syncStatus: SyncStatus)
    suspend fun clear()
    suspend fun deleteLocalTransaction(localTransaction: LocalTransaction)
    suspend fun deleteLocalTransactionById(transactionId: String)

    suspend fun getLocalTransactionCount(): Int

    // Additional transaction-specific methods
    suspend fun getLocalTransactionsByType(transactionType: TransactionType): Flow<List<LocalTransaction>>
    suspend fun getLocalTransactionsByBroker(transactionBroker: TransactionBroker): Flow<List<LocalTransaction>>
    suspend fun getLocalTransactionsByUser(orgUserId: String): Flow<List<LocalTransaction>>
    suspend fun getLocalTransactionsByDateRange(startDate: String, endDate: String): Flow<List<LocalTransaction>>
    suspend fun getUnsyncedTransactions(syncedStatus: SyncStatus = SyncStatus.SYNCED): List<LocalTransaction>
}