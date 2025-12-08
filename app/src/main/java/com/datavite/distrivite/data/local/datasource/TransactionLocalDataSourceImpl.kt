package com.datavite.distrivite.data.local.datasource

import FilterOption
import com.datavite.distrivite.data.local.dao.LocalTransactionDao
import com.datavite.distrivite.data.local.model.LocalTransaction
import com.datavite.distrivite.data.local.model.SyncStatus
import com.datavite.distrivite.utils.TransactionBroker
import com.datavite.distrivite.utils.TransactionType
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TransactionLocalDataSourceImpl @Inject constructor(
    private val localTransactionDao: LocalTransactionDao,
) : TransactionLocalDataSource {
    override suspend fun getLocalTransactionIds(): List<String> {
        return localTransactionDao.getLocalTransactionIds()
    }

    override suspend fun getLocalTransactionsFlow(): Flow<List<LocalTransaction>> {
        return localTransactionDao.getLocalTransactionsAsFlow()
    }

    override suspend fun getLocalTransactionsFor(
        searchQuery: String,
        filterOption: FilterOption
    ): List<LocalTransaction> {
        TODO("Not yet implemented")
    }

    override suspend fun getLocalTransactionsForFilterOption(filterOption: FilterOption): List<LocalTransaction> {
        TODO("Not yet implemented")
    }

    override suspend fun getLocalTransactionById(transactionId: String): LocalTransaction? {
        return localTransactionDao.getLocalTransactionById(transactionId)
    }

    override suspend fun insertLocalTransaction(localTransaction: LocalTransaction) {
        localTransactionDao.saveLocalTransaction(localTransaction)
    }

    override suspend fun saveLocalTransactions(localTransactions: List<LocalTransaction>) {
        localTransactionDao.insertOrUpdateLocalTransactions(localTransactions)
    }

    override suspend fun updateSyncStatus(id: String, syncStatus: SyncStatus) {
        localTransactionDao.updateSyncStatus(id, syncStatus)
    }

    override suspend fun clear() {
        localTransactionDao.clear()
    }

    override suspend fun deleteLocalTransaction(localTransaction: LocalTransaction) {
        localTransactionDao.deleteLocalTransaction(localTransaction.id)
    }

    override suspend fun deleteLocalTransactionById(transactionId: String) {
        localTransactionDao.deleteLocalTransaction(transactionId)
    }

    override suspend fun getLocalTransactionCount(): Int {
        return localTransactionDao.getLocalTransactionCount()
    }

    override suspend fun getLocalTransactionsByType(transactionType: TransactionType): Flow<List<LocalTransaction>> {
        return localTransactionDao.getLocalTransactionsByType(transactionType)
    }

    override suspend fun getLocalTransactionsByBroker(transactionBroker: TransactionBroker): Flow<List<LocalTransaction>> {
        return localTransactionDao.getLocalTransactionsByBroker(transactionBroker)
    }

    override suspend fun getLocalTransactionsByUser(orgUserId: String): Flow<List<LocalTransaction>> {
        return localTransactionDao.getLocalTransactionsByUser(orgUserId)
    }

    override suspend fun getLocalTransactionsByDateRange(startDate: String, endDate: String): Flow<List<LocalTransaction>> {
        return localTransactionDao.getLocalTransactionsByDateRange(startDate, endDate)
    }

    override suspend fun getUnsyncedTransactions(syncedStatus: SyncStatus): List<LocalTransaction> {
        return localTransactionDao.getUnsyncedTransactions(syncedStatus)
    }
}