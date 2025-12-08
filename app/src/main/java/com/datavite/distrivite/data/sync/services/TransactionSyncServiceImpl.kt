package com.datavite.distrivite.data.sync.services

import android.util.Log
import com.datavite.distrivite.data.local.dao.PendingOperationDao
import com.datavite.distrivite.data.local.dao.SyncMetadataDao
import com.datavite.distrivite.data.local.datasource.TransactionLocalDataSource
import com.datavite.distrivite.data.local.model.PendingOperation
import com.datavite.distrivite.data.local.model.SyncStatus
import com.datavite.distrivite.data.mapper.TransactionMapper
import com.datavite.distrivite.data.remote.datasource.TransactionRemoteDataSource
import com.datavite.distrivite.data.remote.model.RemoteTransaction
import com.datavite.distrivite.data.sync.EntityType
import com.datavite.distrivite.data.sync.OperationType
import com.datavite.distrivite.data.sync.SyncConfig
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException
import java.net.HttpURLConnection.*
import javax.inject.Inject

class TransactionSyncServiceImpl @Inject constructor(
    private val remoteDataSource: TransactionRemoteDataSource,
    private val localDataSource: TransactionLocalDataSource,
    private val transactionMapper: TransactionMapper,
    private val syncMetadataDao: SyncMetadataDao,
    private val pendingOperationDao: PendingOperationDao,
) : TransactionSyncService {

    // --- Push CREATE ---
    private suspend fun pushCreatedTransaction(remoteTransaction: RemoteTransaction) {
        try {
            remoteDataSource.createRemoteTransaction(remoteTransaction.orgSlug, remoteTransaction)
            val updatedDomain = transactionMapper.mapRemoteToDomain(remoteTransaction)
            val localEntity = transactionMapper.mapDomainToLocal(updatedDomain)

            localDataSource.insertLocalTransaction(localEntity)
            Log.i("TransactionSync", "Successfully synced created transaction ${remoteTransaction.id}")
        } catch (e: Exception) {
            handleTransactionSyncException(e, remoteTransaction.id, "CREATE")
            throw e
        }
    }

    // --- Push UPDATE ---
    private suspend fun pushUpdatedTransaction(remoteTransaction: RemoteTransaction) {
        try {
            remoteDataSource.updateRemoteTransaction(remoteTransaction.orgSlug, remoteTransaction)
            val updatedDomain = transactionMapper.mapRemoteToDomain(remoteTransaction)
            val localEntity = transactionMapper.mapDomainToLocal(updatedDomain)

            localDataSource.insertLocalTransaction(localEntity)
            Log.i("TransactionSync", "Successfully synced updated transaction ${remoteTransaction.id}")
        } catch (e: Exception) {
            handleTransactionSyncException(e, remoteTransaction.id, "UPDATE")
            throw e
        }
    }

    // --- Push DELETE ---
    private suspend fun pushDeletedTransaction(remoteTransaction: RemoteTransaction) {
        try {
            remoteDataSource.deleteRemoteTransaction(remoteTransaction.orgSlug, remoteTransaction.id)
            Log.i("TransactionSync", "Successfully synced deleted transaction ${remoteTransaction.id}")
        } catch (e: Exception) {
            handleTransactionSyncException(e, remoteTransaction.id, "DELETE")
            throw e
        }
    }

    // --- Comprehensive Exception Handling ---
    private suspend fun handleTransactionSyncException(e: Exception, transactionId: String, operation: String) {
        when (e) {
            is HttpException -> {
                when (e.code()) {
                    HTTP_NOT_FOUND -> handleNotFound(transactionId, operation)
                    HTTP_CONFLICT -> handleConflict(transactionId, operation)
                    HTTP_UNAVAILABLE -> handleServiceUnavailable(transactionId, operation)
                    HTTP_INTERNAL_ERROR -> handleServerError(transactionId, operation)
                    HTTP_BAD_REQUEST -> handleBadRequest(transactionId, operation)
                    HTTP_UNAUTHORIZED -> handleUnauthorized(transactionId, operation)
                    HTTP_FORBIDDEN -> handleForbidden(transactionId, operation)
                    HTTP_BAD_GATEWAY -> handleBadGateway(transactionId, operation)
                    HTTP_GATEWAY_TIMEOUT -> handleGatewayTimeout(transactionId, operation)
                    in 400..499 -> handleClientError(transactionId, operation, e.code())
                    in 500..599 -> handleServerError(transactionId, operation, e.code())
                    else -> handleGenericHttpError(transactionId, operation, e.code())
                }
            }
            is IOException -> handleNetworkError(transactionId, operation, e)
            else -> handleUnknownError(transactionId, operation, e)
        }
    }

    // --- HTTP Status Code Handlers ---
    private suspend fun handleNotFound(transactionId: String, operation: String) {
        Log.i("TransactionSync", "Transaction $transactionId not found during $operation - removing locally")
        handleDeletedTransaction(transactionId, operation)
    }

    private fun handleConflict(transactionId: String, operation: String) {
        Log.w("TransactionSync", "Conflict detected for transaction $transactionId during $operation - requires resolution")
    }

    private fun handleServiceUnavailable(transactionId: String, operation: String) {
        Log.w("TransactionSync", "Service unavailable for transaction $transactionId during $operation - retry later")
    }

    private fun handleServerError(transactionId: String, operation: String, statusCode: Int? = null) {
        val codeInfo = if (statusCode != null) " (code: $statusCode)" else ""
        Log.e("TransactionSync", "Server error for transaction $transactionId during $operation$codeInfo")
    }

    private fun handleBadRequest(transactionId: String, operation: String) {
        Log.e("TransactionSync", "Bad request for transaction $transactionId during $operation - check data format")
    }

    private fun handleUnauthorized(transactionId: String, operation: String) {
        Log.e("TransactionSync", "Unauthorized for transaction $transactionId during $operation - authentication required")
    }

    private fun handleForbidden(transactionId: String, operation: String) {
        Log.e("TransactionSync", "Forbidden for transaction $transactionId during $operation - insufficient permissions")
    }

    private fun handleBadGateway(transactionId: String, operation: String) {
        Log.w("TransactionSync", "Bad gateway for transaction $transactionId during $operation - retry later")
    }

    private fun handleGatewayTimeout(transactionId: String, operation: String) {
        Log.w("TransactionSync", "Gateway timeout for transaction $transactionId during $operation - retry later")
    }

    private fun handleClientError(transactionId: String, operation: String, statusCode: Int) {
        Log.e("TransactionSync", "Client error $statusCode for transaction $transactionId during $operation")
    }

    private fun handleGenericHttpError(transactionId: String, operation: String, statusCode: Int) {
        Log.e("TransactionSync", "HTTP error $statusCode for transaction $transactionId during $operation")
    }

    // --- Network and Generic Error Handlers ---
    private fun handleNetworkError(transactionId: String, operation: String, e: IOException) {
        Log.w("TransactionSync", "Network error for transaction $transactionId during $operation: ${e.message}")
    }

    private fun handleUnknownError(transactionId: String, operation: String, e: Exception) {
        Log.e("TransactionSync", "Unknown error for transaction $transactionId during $operation: ${e.message}", e)
    }

    // --- Handle deleted transaction (404 scenario) ---
    private suspend fun handleDeletedTransaction(transactionId: String, operationType: String) {
        try {
            localDataSource.deleteLocalTransactionById(transactionId)
            Log.i("TransactionSync", "Transaction $transactionId was deleted on server during $operationType, removed locally")
        } catch (e: Exception) {
            Log.e("TransactionSync", "Failed to clean up locally deleted transaction $transactionId", e)
        }
    }

    // --- Push pending operations ---
    override suspend fun push(operations: List<PendingOperation>) {
        for (operation in operations) {
            syncOperation(operation, operations)
        }
    }

    override suspend fun hasCachedData(): Boolean = localDataSource.getLocalTransactionCount() != 0

    private suspend fun syncOperation(currentOperation: PendingOperation, allOperations: List<PendingOperation>) {
        val transaction = currentOperation.parsePayload<RemoteTransaction>()
        val totalPending = allOperations.count { it.entityId == currentOperation.entityId }

        localDataSource.updateSyncStatus(currentOperation.entityId, SyncStatus.SYNCING)

        try {
            when (currentOperation.operationType) {
                OperationType.CREATE -> pushCreatedTransaction(transaction)
                OperationType.UPDATE -> pushUpdatedTransaction(transaction)
                OperationType.DELETE -> pushDeletedTransaction(transaction)
                else -> {} // ignore other types
            }

            pendingOperationDao.deleteByKeys(
                entityType = currentOperation.entityType,
                entityId = currentOperation.entityId,
                operationType = currentOperation.operationType,
                orgId = currentOperation.orgId
            )

            val newStatus = if ((totalPending - 1) > 0) SyncStatus.PENDING else SyncStatus.SYNCED
            localDataSource.updateSyncStatus(currentOperation.entityId, newStatus)

        } catch (e: Exception) {
            val isNotFoundError = e is HttpException && e.code() == HTTP_NOT_FOUND
            if (!isNotFoundError) {
                pendingOperationDao.incrementFailureCount(
                    entityType = currentOperation.entityType,
                    entityId = currentOperation.entityId,
                    operationType = currentOperation.operationType,
                    orgId = currentOperation.orgId
                )
                val failureCount = pendingOperationDao.getFailureCount(
                    entityType = currentOperation.entityType,
                    entityId = currentOperation.entityId,
                    operationType = currentOperation.operationType,
                    orgId = currentOperation.orgId
                )
                val status = if (failureCount > 5) SyncStatus.FAILED else SyncStatus.PENDING
                localDataSource.updateSyncStatus(currentOperation.entityId, status)
            }

            if (!isNotFoundError) {
                Log.e("TransactionSyncOperation", "Failed operation ${currentOperation.operationType} ${currentOperation.entityType} ${currentOperation.entityId}", e)
            }
        }
    }

    override fun getEntity(): EntityType = EntityType.Transaction

    // --- Sync Logic ---
    private fun shouldPerformFullSync(lastSync: Long?): Boolean {
        return when {
            lastSync == null -> true
            System.currentTimeMillis() - lastSync > SyncConfig.FULL_SYNC_THRESHOLD_MS -> true
            else -> false
        }
    }

    private suspend fun getLastSyncTimestamp(): Long? {
        return syncMetadataDao.getLastSyncTimestamp(EntityType.Transaction)
    }

    private suspend fun updateLastSyncTimestamp(timestamp: Long, success: Boolean = true, error: String? = null) {
        syncMetadataDao.updateLastSync(EntityType.Transaction, timestamp, success)
        if (error != null) {
            syncMetadataDao.updateSyncStatus(EntityType.Transaction, false, error)
        }
    }

    private suspend fun processIncrementalChanges(organization: String, since: Long) {
        try {
            Log.i("TransactionSync", "Performing incremental sync since ${java.util.Date(since)}")

            val adjustedSince = since - (45 * 60 * 1000) // 45 minutes buffer

            val changes = remoteDataSource.getRemoteTransactionsChangesSince(organization, adjustedSince)
            val remoteTransactionIds = remoteDataSource.getRemoteTransactionIds(organization)

            val domainTransactions = changes.map { transactionMapper.mapRemoteToDomain(it) }
            val localEntities = domainTransactions.map { transactionMapper.mapDomainToLocal(it) }

            localEntities.forEach { entity ->
                localDataSource.insertLocalTransaction(entity)
            }

            cleanupDeletedTransactions(organization, remoteTransactionIds)

            Log.i("TransactionSync", "Incremental sync completed: ${changes.size} updates")

        } catch (e: Exception) {
            Log.w("TransactionSync", "Incremental sync failed, will fall back to full sync", e)
            throw e
        }
    }

    private suspend fun processFullSync(organization: String) {
        Log.i("TransactionSync", "Performing full sync")
        val remoteTransactions = remoteDataSource.getRemoteTransactions(organization)
        val remoteTransactionIds = remoteDataSource.getRemoteTransactionIds(organization)

        val domainTransactions = remoteTransactions.map { transactionMapper.mapRemoteToDomain(it) }
        val localEntities = domainTransactions.map { transactionMapper.mapDomainToLocal(it) }

        localEntities.forEach {
            localDataSource.insertLocalTransaction(it)
        }

        cleanupDeletedTransactions(organization, remoteTransactionIds)

        Log.i("TransactionSync", "Full sync completed: ${localEntities.size} transactions")
    }

    // --- Full pull from remote ---
    override suspend fun pullAll(organization: String) {
        Log.i("TransactionSync", "Sync started for organization: $organization")

        val lastSync = getLastSyncTimestamp()
        val shouldFullSync = shouldPerformFullSync(lastSync)

        Log.w("TransactionSync", "LastSync ${lastSync.toString()} should sync: $shouldFullSync")

        var success = false
        var incrementalAttempts = 0
        var syncError: String? = null

        while (!success && incrementalAttempts <= SyncConfig.MAX_INCREMENTAL_RETRY_COUNT) {
            try {
                if (shouldFullSync || incrementalAttempts > 0) {
                    processFullSync(organization)
                } else {
                    lastSync?.let { processIncrementalChanges(organization, it) }
                }
                success = true

            } catch (e: Exception) {
                incrementalAttempts++
                syncError = e.message

                if (incrementalAttempts > SyncConfig.MAX_INCREMENTAL_RETRY_COUNT) {
                    Log.w("TransactionSync", "Incremental sync failed after $incrementalAttempts attempts, forcing full sync")
                    try {
                        processFullSync(organization)
                        success = true
                    } catch (fullSyncError: Exception) {
                        syncError = fullSyncError.message
                        Log.e("TransactionSync", "Full sync also failed", fullSyncError)
                    }
                } else {
                    Log.w("TransactionSync", "Incremental sync attempt $incrementalAttempts failed, retrying...", e)
                    delay(1000L * incrementalAttempts)
                }
            }
        }

        if (success) {
            updateLastSyncTimestamp(System.currentTimeMillis(), true)
            Log.i("TransactionSync", "Sync completed successfully")
        } else {
            updateLastSyncTimestamp(lastSync ?: 0L, false, syncError)
            Log.e("TransactionSync", "Sync failed after all attempts")
            throw RuntimeException("Sync failed: $syncError")
        }
    }

    private suspend fun cleanupDeletedTransactions(
        organization: String,
        allRemoteIds: List<String>,
        maxDeletions: Int = 500
    ): CleanupResult {
        try {
            Log.d("TransactionSync", "Starting deletion cleanup for $organization...")

            val localIds = localDataSource.getLocalTransactionIds().toSet()
            val remoteIdsSet = allRemoteIds.toSet()
            val mustBeDeletedLocally = localIds.subtract(remoteIdsSet)

            return if (mustBeDeletedLocally.isNotEmpty()) {
                val idsToDelete = if (mustBeDeletedLocally.size > maxDeletions) {
                    Log.w("TransactionSync", "Too many deletions (${mustBeDeletedLocally.size}), limiting to $maxDeletions")
                    mustBeDeletedLocally.take(maxDeletions)
                } else {
                    mustBeDeletedLocally.toList()
                }

                Log.i("TransactionSync", "Found ${mustBeDeletedLocally.size} transactions to delete (processing ${idsToDelete.size})")

                idsToDelete.forEach { deletedId ->
                    localDataSource.deleteLocalTransactionById(deletedId)
                }

                CleanupResult(
                    success = true,
                    deletedCount = idsToDelete.size,
                    totalFound = mustBeDeletedLocally.size,
                    limited = mustBeDeletedLocally.size > maxDeletions
                ).also {
                    Log.i("TransactionSync", "Deletion cleanup completed: $it")
                }

            } else {
                CleanupResult(success = true, deletedCount = 0).also {
                    Log.d("TransactionSync", "No deleted transactions found")
                }
            }

        } catch (e: Exception) {
            Log.e("TransactionSync", "Failed during deletion cleanup", e)
            return CleanupResult(success = false, error = e.message)
        }
    }

    data class CleanupResult(
        val success: Boolean,
        val deletedCount: Int = 0,
        val totalFound: Int = 0,
        val limited: Boolean = false,
        val error: String? = null
    ) {
        override fun toString(): String {
            return "CleanupResult(deleted=$deletedCount, totalFound=$totalFound, limited=$limited, success=$success)"
        }
    }
}