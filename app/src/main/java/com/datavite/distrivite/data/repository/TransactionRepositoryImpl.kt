package com.datavite.distrivite.data.repository

import FilterOption
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import com.datavite.distrivite.data.local.dao.PendingNotificationDao
import com.datavite.distrivite.data.local.dao.PendingOperationDao
import com.datavite.distrivite.data.local.datasource.TransactionLocalDataSource
import com.datavite.distrivite.data.local.model.PendingOperation
import com.datavite.distrivite.data.local.model.SyncStatus
import com.datavite.distrivite.data.mapper.TransactionMapper
import com.datavite.distrivite.data.remote.datasource.TransactionRemoteDataSource
import com.datavite.distrivite.data.sync.EntityType
import com.datavite.distrivite.data.sync.OperationType
import com.datavite.distrivite.domain.model.DomainTransaction
import com.datavite.distrivite.domain.notification.NotificationBus
import com.datavite.distrivite.domain.notification.NotificationEvent
import com.datavite.distrivite.domain.repository.TransactionRepository
import com.datavite.distrivite.utils.JsonConverter
import com.datavite.distrivite.utils.TransactionBroker
import com.datavite.distrivite.utils.TransactionType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject

class TransactionRepositoryImpl @Inject constructor(
    private val localDataSource: TransactionLocalDataSource,
    private val remoteDataSource: TransactionRemoteDataSource,
    private val transactionMapper: TransactionMapper,
    private val pendingOperationDao: PendingOperationDao,
    private val pendingNotificationDao: PendingNotificationDao,
    private val notificationBus: NotificationBus
) : TransactionRepository {

    override suspend fun getDomainTransactionsFlow(): Flow<List<DomainTransaction>> {
        return localDataSource.getLocalTransactionsFlow().map { localTransactions ->
            localTransactions.map { transactionMapper.mapLocalToDomain(it) }
        }
    }

    override suspend fun getDomainTransactionById(domainTransactionId: String): DomainTransaction? {
        val localTransaction = localDataSource.getLocalTransactionById(domainTransactionId)
        return localTransaction?.let { transactionMapper.mapLocalToDomain(it) }
    }

    override suspend fun createTransaction(domainTransaction: DomainTransaction) {
        val pendingDomainTransaction = domainTransaction.copy(
            created = LocalDateTime.now().toString(),
            modified = LocalDateTime.now().toString(),
        )

        val local = transactionMapper.mapDomainToLocal(pendingDomainTransaction)
        val remote = transactionMapper.mapDomainToRemote(pendingDomainTransaction)

        val operation = PendingOperation(
            orgSlug = domainTransaction.orgSlug,
            orgId = domainTransaction.orgId,
            entityId = domainTransaction.id,
            entityType = EntityType.Transaction,
            operationType = OperationType.CREATE,
            payloadJson = JsonConverter.toJson(remote),
        )

        try {
            localDataSource.insertLocalTransaction(local)
            pendingOperationDao.insert(operation)
            notificationBus.emit(NotificationEvent.Success("Transaction created successfully"))
        } catch (e: SQLiteConstraintException) {
            notificationBus.emit(NotificationEvent.Failure("Another transaction with the same ID already exists"))
        }
    }

    private suspend fun updateTransaction(domainTransaction: DomainTransaction) {
        val pendingDomainTransaction = domainTransaction.copy(syncStatus = SyncStatus.PENDING)
        val local = transactionMapper.mapDomainToLocal(pendingDomainTransaction)
        val remote = transactionMapper.mapDomainToRemote(pendingDomainTransaction)

        val operation = PendingOperation(
            orgSlug = domainTransaction.orgSlug,
            orgId = domainTransaction.orgId,
            entityId = domainTransaction.id,
            entityType = EntityType.Transaction,
            operationType = OperationType.UPDATE,
            payloadJson = JsonConverter.toJson(remote),
        )

        localDataSource.insertLocalTransaction(local)
        pendingOperationDao.insert(operation)
    }

    override suspend fun deleteTransaction(domainTransaction: DomainTransaction) {
        val pendingDomainTransaction = domainTransaction.copy(syncStatus = SyncStatus.PENDING)
        val local = transactionMapper.mapDomainToLocal(pendingDomainTransaction)
        val remote = transactionMapper.mapDomainToRemote(pendingDomainTransaction)

        val operation = PendingOperation(
            orgSlug = domainTransaction.orgSlug,
            orgId = domainTransaction.orgId,
            entityId = domainTransaction.id,
            entityType = EntityType.Transaction,
            operationType = OperationType.DELETE,
            payloadJson = JsonConverter.toJson(remote),
        )

        localDataSource.deleteLocalTransaction(local)
        pendingOperationDao.insert(operation)
    }

    override suspend fun fetchIfEmpty(organization: String) {
        try {
            if (localDataSource.getLocalTransactionCount() == 0) {
                val remoteTransactions = remoteDataSource.getRemoteTransactions(organization)
                val domainTransactions = remoteTransactions.map { transactionMapper.mapRemoteToDomain(it) }
                val localTransactions = domainTransactions.map { transactionMapper.mapDomainToLocal(it) }
                localDataSource.clear()
                localDataSource.saveLocalTransactions(localTransactions)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("TransactionRepository", "Error fetching transactions: ${e.message}")
        }
    }

    override suspend fun getDomainTransactionsFor(
        searchQuery: String,
        filterOption: FilterOption
    ): List<DomainTransaction> {
        TODO("Not yet implemented")
    }

    override suspend fun getDomainTransactionsForFilterOption(filterOption: FilterOption): List<DomainTransaction> {
        TODO("Not yet implemented")
    }

    // Transaction-specific methods
    override suspend fun getDomainTransactionsByType(transactionType: TransactionType): Flow<List<DomainTransaction>> {
        return localDataSource.getLocalTransactionsByType(transactionType).map { localTransactions ->
            localTransactions.map { transactionMapper.mapLocalToDomain(it) }
        }
    }

    override suspend fun getDomainTransactionsByBroker(transactionBroker: TransactionBroker): Flow<List<DomainTransaction>> {
        return localDataSource.getLocalTransactionsByBroker(transactionBroker).map { localTransactions ->
            localTransactions.map { transactionMapper.mapLocalToDomain(it) }
        }
    }

    override suspend fun getDomainTransactionsByUser(orgUserId: String): Flow<List<DomainTransaction>> {
        return localDataSource.getLocalTransactionsByUser(orgUserId).map { localTransactions ->
            localTransactions.map { transactionMapper.mapLocalToDomain(it) }
        }
    }

    override suspend fun getDomainTransactionsByDateRange(startDate: String, endDate: String): Flow<List<DomainTransaction>> {
        return localDataSource.getLocalTransactionsByDateRange(startDate, endDate).map { localTransactions ->
            localTransactions.map { transactionMapper.mapLocalToDomain(it) }
        }
    }
}