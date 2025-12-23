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
import com.datavite.distrivite.data.sync.OperationScope
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
    private suspend fun pushCreatedTransaction(remoteTransaction: RemoteTransaction, currentOperation: PendingOperation) {
        try {
            remoteDataSource.createRemoteTransaction(remoteTransaction.orgSlug, remoteTransaction)
            val updatedDomain = transactionMapper.mapRemoteToDomain(remoteTransaction)
            val localEntity = transactionMapper.mapDomainToLocal(updatedDomain)

            localDataSource.insertLocalTransaction(localEntity)
            Log.i("TransactionSync", "Successfully synced created transaction ${remoteTransaction.id}")
        } catch (e: Exception) {
            val handled = handleTransactionSyncException(e, remoteTransaction.id, currentOperation)
            if (!handled) throw e
        }
    }

    // --- Push UPDATE ---
    private suspend fun pushUpdatedTransaction(remoteTransaction: RemoteTransaction, currentOperation: PendingOperation) {
        try {
            remoteDataSource.updateRemoteTransaction(remoteTransaction.orgSlug, remoteTransaction)
            val updatedDomain = transactionMapper.mapRemoteToDomain(remoteTransaction)
            val localEntity = transactionMapper.mapDomainToLocal(updatedDomain)

            localDataSource.insertLocalTransaction(localEntity)
            Log.i("TransactionSync", "Successfully synced updated transaction ${remoteTransaction.id}")
        } catch (e: Exception) {
            val handled = handleTransactionSyncException(e, remoteTransaction.id, currentOperation)
            if (!handled) throw e
        }
    }

    // --- Push DELETE ---
    private suspend fun pushDeletedTransaction(remoteTransaction: RemoteTransaction, currentOperation: PendingOperation) {
        try {
            remoteDataSource.deleteRemoteTransaction(remoteTransaction.orgSlug, remoteTransaction.id)
            Log.i("TransactionSync", "Successfully synced deleted transaction ${remoteTransaction.id}")
        } catch (e: Exception) {
            val handled = handleTransactionSyncException(e, remoteTransaction.id, currentOperation)
            if (!handled) throw e
        }
    }

    // --- Comprehensive Exception Handling ---
    private suspend fun handleTransactionSyncException(e: Exception, transactionId: String, currentOperation: PendingOperation): Boolean {
        return when (e) {
            is HttpException -> {
                when (e.code()) {
                    HTTP_NOT_FOUND -> handleNotFound(transactionId, currentOperation)
                    HTTP_CONFLICT -> handleConflict(transactionId, currentOperation)
                    HTTP_UNAVAILABLE -> handleServiceUnavailable(transactionId, currentOperation)
                    HTTP_INTERNAL_ERROR -> handleServerError(transactionId, currentOperation)
                    HTTP_BAD_REQUEST -> handleBadRequest(transactionId, currentOperation)
                    HTTP_UNAUTHORIZED -> handleUnauthorized(transactionId, currentOperation)
                    HTTP_FORBIDDEN -> handleForbidden(transactionId, currentOperation)
                    HTTP_BAD_GATEWAY -> handleBadGateway(transactionId, currentOperation)
                    HTTP_GATEWAY_TIMEOUT -> handleGatewayTimeout(transactionId, currentOperation)
                    in 400..499 -> handleClientError(transactionId, currentOperation, e.code())
                    in 500..599 -> handleServerError(transactionId, currentOperation, e.code())
                    else -> handleGenericHttpError(transactionId, currentOperation, e.code())
                }
            }
            is IOException -> handleNetworkError(transactionId, currentOperation, e)
            else -> handleUnknownError(transactionId, currentOperation, e)
        }
    }

    // --- HTTP Status Code Handlers ---
    private suspend fun handleNotFound(transactionId: String, operation: PendingOperation): Boolean {
        Log.i("TransactionSync", "Transaction $transactionId not found during ${operation.operationType} - removing locally")
        return try {
            localDataSource.deleteLocalTransactionById(transactionId)
            Log.i("TransactionSync", "Transaction $transactionId was deleted on server, removed locally")
            true  // Exception handled successfully
        } catch (e: Exception) {
            Log.e("TransactionSync", "Failed to clean up locally deleted transaction $transactionId", e)
            false  // Exception not fully handled
        }
    }

    private suspend fun handleConflict(transactionId: String, operation: PendingOperation): Boolean {
        Log.w("TransactionSync", "Conflict detected for transaction $transactionId during ${operation.operationType} - resolving...")

        return when (operation.operationType) {
            OperationType.CREATE -> resolveCreateConflict(transactionId, operation)
            OperationType.UPDATE -> resolveUpdateConflict(transactionId, operation)
            OperationType.DELETE -> resolveDeleteConflict(transactionId, operation)
            else -> {
                Log.w("TransactionSync", "Unhandled conflict type for ${operation.operationType}")
                false
            }
        }
    }

    private suspend fun resolveCreateConflict(transactionId: String, operation: PendingOperation): Boolean {
        try {
            // For CREATE conflict, transaction already exists on server
            // Mark as synced since the data is already on the server
            localDataSource.updateSyncStatus(operation.entityId, SyncStatus.SYNCED)
            Log.w("TransactionSync", "CREATE conflict resolved for $transactionId - marked as synced")
            return true
        } catch (e: Exception) {
            Log.e("TransactionSync", "Failed to resolve CREATE conflict for $transactionId", e)
            return false
        }
    }

    private suspend fun resolveUpdateConflict(transactionId: String, operation: PendingOperation): Boolean {
        Log.w("TransactionSync", "UPDATE conflict for $transactionId - keeping as pending for retry")
        return false  // Not handled, will retry
    }

    private suspend fun resolveDeleteConflict(transactionId: String, operation: PendingOperation): Boolean {
        try {
            // For DELETE conflict, item might already be deleted
            localDataSource.deleteLocalTransactionById(transactionId)
            Log.i("TransactionSync", "DELETE conflict resolved for $transactionId - removed locally")
            return true
        } catch (e: Exception) {
            Log.e("TransactionSync", "Failed to resolve DELETE conflict for $transactionId", e)
            return false
        }
    }

    private fun handleServiceUnavailable(transactionId: String, operation: PendingOperation): Boolean {
        Log.w("TransactionSync", "Service unavailable for transaction $transactionId during ${operation.operationType} - retry later")
        return false  // Not handled, will retry
    }

    private fun handleServerError(transactionId: String, operation: PendingOperation, statusCode: Int? = null): Boolean {
        val codeInfo = if (statusCode != null) " (code: $statusCode)" else ""
        Log.e("TransactionSync", "Server error for transaction $transactionId during ${operation.operationType}$codeInfo")
        return false  // Not handled, will retry
    }

    private fun handleBadRequest(transactionId: String, operation: PendingOperation): Boolean {
        Log.e("TransactionSync", "Bad request for transaction $transactionId during ${operation.operationType} - check data format")
        return true  // Handled - bad request won't succeed on retry
    }

    private fun handleUnauthorized(transactionId: String, operation: PendingOperation): Boolean {
        Log.e("TransactionSync", "Unauthorized for transaction $transactionId during ${operation.operationType} - authentication required")
        return true  // Handled - auth error needs user intervention
    }

    private fun handleForbidden(transactionId: String, operation: PendingOperation): Boolean {
        Log.e("TransactionSync", "Forbidden for transaction $transactionId during ${operation.operationType} - insufficient permissions")
        return true  // Handled - permission error won't succeed on retry
    }

    private fun handleBadGateway(transactionId: String, operation: PendingOperation): Boolean {
        Log.w("TransactionSync", "Bad gateway for transaction $transactionId during ${operation.operationType} - retry later")
        return false  // Not handled, will retry
    }

    private fun handleGatewayTimeout(transactionId: String, operation: PendingOperation): Boolean {
        Log.w("TransactionSync", "Gateway timeout for transaction $transactionId during ${operation.operationType} - retry later")
        return false  // Not handled, will retry
    }

    private fun handleClientError(transactionId: String, operation: PendingOperation, statusCode: Int): Boolean {
        Log.e("TransactionSync", "Client error $statusCode for transaction $transactionId during ${operation.operationType}")
        return true  // Handled - client errors won't succeed on retry without fixing
    }

    private fun handleGenericHttpError(transactionId: String, operation: PendingOperation, statusCode: Int): Boolean {
        Log.e("TransactionSync", "HTTP error $statusCode for transaction $transactionId during ${operation.operationType}")
        return false  // Not handled, generic case
    }

    // --- Network and Generic Error Handlers ---
    private fun handleNetworkError(transactionId: String, operation: PendingOperation, e: IOException): Boolean {
        Log.w("TransactionSync", "Network error for transaction $transactionId during ${operation.operationType}: ${e.message}")
        return false  // Not handled, will retry
    }

    private fun handleUnknownError(transactionId: String, operation: PendingOperation, e: Exception): Boolean {
        Log.e("TransactionSync", "Unknown error for transaction $transactionId during ${operation.operationType}: ${e.message}", e)
        return false  // Not handled
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
                OperationType.CREATE -> pushCreatedTransaction(transaction, currentOperation)
                OperationType.UPDATE -> pushUpdatedTransaction(transaction, currentOperation)
                OperationType.DELETE -> pushDeletedTransaction(transaction, currentOperation)
                else -> {} // ignore other types
            }

            // If we get here without throwing, operation succeeded
            when(currentOperation.operationScope){
                OperationScope.STATE -> {
                    pendingOperationDao.deleteByKeys(
                        entityType = currentOperation.entityType,
                        entityId = currentOperation.entityId,
                        operationType = currentOperation.operationType,
                        orgId = currentOperation.orgId
                    )
                }
                OperationScope.EVENT -> {
                    pendingOperationDao.deleteById(currentOperation.id)
                }
            }

            updateFinalStatus(currentOperation.entityId)

        } catch (e: Exception) {
            // Exception was either not handled or handler said to rethrow
            handleSyncFailure(currentOperation, e)
        }
    }

    private suspend fun updateFinalStatus(entityId: String) {
        val remaining = pendingOperationDao.countForEntity(entityId)
        val status = if (remaining > 0) SyncStatus.PENDING else SyncStatus.SYNCED
        localDataSource.updateSyncStatus(entityId, status)
    }

    private suspend fun handleSyncFailure(currentOperation: PendingOperation, e: Exception) {
        val isNotFoundError = e is HttpException && e.code() == HTTP_NOT_FOUND
        val isHandledError = when {
            isNotFoundError -> true  // Already handled in handleNotFound()
            e is HttpException && e.code() == HTTP_CONFLICT -> {
                // Check if conflict was handled
                when (currentOperation.operationType) {
                    OperationType.CREATE, OperationType.DELETE -> true  // Handled in resolve methods
                    else -> false  // UPDATE conflict not handled
                }
            }
            else -> false
        }

        if (!isHandledError) {
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

            Log.e("TransactionSyncOperation", "Failed operation ${currentOperation.operationType} ${currentOperation.entityType} ${currentOperation.entityId}", e)
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