package com.datavite.distrivite.data.sync.services

import android.util.Log
import com.datavite.distrivite.data.local.dao.PendingOperationDao
import com.datavite.distrivite.data.local.dao.SyncMetadataDao
import com.datavite.distrivite.data.local.datasource.BulkCreditPaymentLocalDataSource
import com.datavite.distrivite.data.local.model.PendingOperation
import com.datavite.distrivite.data.local.model.SyncStatus
import com.datavite.distrivite.data.mapper.BulkCreditPaymentMapper
import com.datavite.distrivite.data.remote.datasource.BulkCreditPaymentRemoteDataSource
import com.datavite.distrivite.data.remote.model.RemoteBulkCreditPayment
import com.datavite.distrivite.data.sync.EntityType
import com.datavite.distrivite.data.sync.OperationScope
import com.datavite.distrivite.data.sync.OperationType
import com.datavite.distrivite.data.sync.SyncConfig
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException
import java.net.HttpURLConnection.*
import javax.inject.Inject

class BulkCreditPaymentSyncServiceImpl @Inject constructor(
    private val remoteDataSource: BulkCreditPaymentRemoteDataSource,
    private val localDataSource: BulkCreditPaymentLocalDataSource,
    private val bulkCreditPaymentMapper: BulkCreditPaymentMapper,
    private val syncMetadataDao: SyncMetadataDao,
    private val pendingOperationDao: PendingOperationDao,
) : BulkCreditPaymentSyncService {

    // --- Push CREATE ---
    private suspend fun pushCreatedBulkCreditPayment(
        remoteBulkCreditPayment: RemoteBulkCreditPayment,
        currentOperation: PendingOperation
    ) {
        try {
            remoteDataSource.createRemoteBulkCreditPayment(
                remoteBulkCreditPayment.orgSlug,
                remoteBulkCreditPayment
            )
            val updatedDomain = bulkCreditPaymentMapper.mapRemoteToDomain(remoteBulkCreditPayment)
            val localEntity = bulkCreditPaymentMapper.mapDomainToLocal(updatedDomain)
            localDataSource.insertLocalBulkCreditPayment(localEntity)
            Log.i("BulkCreditPaymentSync", "Successfully synced created bulk credit payment ${remoteBulkCreditPayment.id}")
        } catch (e: Exception) {
            val handled = handleBulkCreditPaymentSyncException(e, remoteBulkCreditPayment.id, "CREATE", currentOperation)
            if (!handled) throw e
        }
    }

    // --- Push UPDATE ---
    private suspend fun pushUpdatedBulkCreditPayment(
        remoteBulkCreditPayment: RemoteBulkCreditPayment,
        currentOperation: PendingOperation
    ) {
        try {
            remoteDataSource.updateRemoteBulkCreditPayment(
                remoteBulkCreditPayment.orgSlug,
                remoteBulkCreditPayment
            )
            val updatedDomain = bulkCreditPaymentMapper.mapRemoteToDomain(remoteBulkCreditPayment)
            val localEntity = bulkCreditPaymentMapper.mapDomainToLocal(updatedDomain)
            localDataSource.insertLocalBulkCreditPayment(localEntity)
            Log.i("BulkCreditPaymentSync", "Successfully synced updated bulk credit payment ${remoteBulkCreditPayment.id}")
        } catch (e: Exception) {
            val handled = handleBulkCreditPaymentSyncException(e, remoteBulkCreditPayment.id, "UPDATE", currentOperation)
            if (!handled) throw e
        }
    }

    // --- Push DELETE ---
    private suspend fun pushDeletedBulkCreditPayment(
        remoteBulkCreditPayment: RemoteBulkCreditPayment,
        currentOperation: PendingOperation
    ) {
        try {
            remoteDataSource.deleteRemoteBulkCreditPayment(
                remoteBulkCreditPayment.orgSlug,
                remoteBulkCreditPayment.id
            )
            Log.i("BulkCreditPaymentSync", "Successfully synced deleted bulk credit payment ${remoteBulkCreditPayment.id}")
        } catch (e: Exception) {
            val handled = handleBulkCreditPaymentSyncException(e, remoteBulkCreditPayment.id, "DELETE", currentOperation)
            if (!handled) throw e
        }
    }

    // --- Comprehensive Exception Handling ---
    private suspend fun handleBulkCreditPaymentSyncException(
        e: Exception,
        paymentId: String,
        operation: String,
        currentOperation: PendingOperation
    ): Boolean {
        return when (e) {
            is HttpException -> {
                when (e.code()) {
                    HTTP_NOT_FOUND -> handleNotFound(paymentId, operation, currentOperation)
                    HTTP_CONFLICT -> handleConflict(paymentId, operation, currentOperation)
                    HTTP_UNAVAILABLE -> handleServiceUnavailable(paymentId, operation, currentOperation)
                    HTTP_INTERNAL_ERROR -> handleServerError(paymentId, operation, currentOperation)
                    HTTP_BAD_REQUEST -> handleBadRequest(paymentId, operation, currentOperation)
                    HTTP_UNAUTHORIZED -> handleUnauthorized(paymentId, operation, currentOperation)
                    HTTP_FORBIDDEN -> handleForbidden(paymentId, operation, currentOperation)
                    HTTP_BAD_GATEWAY -> handleBadGateway(paymentId, operation, currentOperation)
                    HTTP_GATEWAY_TIMEOUT -> handleGatewayTimeout(paymentId, operation, currentOperation)
                    in 400..499 -> handleClientError(paymentId, operation, e.code(), currentOperation)
                    in 500..599 -> handleServerError(paymentId, operation, currentOperation, e.code())
                    else -> handleGenericHttpError(paymentId, operation, e.code(), currentOperation)
                }
            }
            is IOException -> handleNetworkError(paymentId, operation, e, currentOperation)
            else -> handleUnknownError(paymentId, operation, e, currentOperation)
        }
    }

    // --- HTTP Status Code Handlers ---
    private suspend fun handleNotFound(
        paymentId: String,
        operation: String,
        currentOperation: PendingOperation
    ): Boolean {
        Log.i("BulkCreditPaymentSync", "Bulk credit payment $paymentId not found during $operation - removing locally")
        return try {
            localDataSource.deleteLocalBulkCreditPaymentById(paymentId)
            Log.i("BulkCreditPaymentSync", "Bulk credit payment $paymentId was deleted on server, removed locally")
            true
        } catch (e: Exception) {
            Log.e("BulkCreditPaymentSync", "Failed to clean up locally deleted bulk credit payment $paymentId", e)
            false
        }
    }

    private suspend fun handleConflict(
        paymentId: String,
        operation: String,
        currentOperation: PendingOperation
    ): Boolean {
        Log.w("BulkCreditPaymentSync", "Conflict detected for bulk credit payment $paymentId during $operation - resolving...")
        return when (currentOperation.operationType) {
            OperationType.CREATE -> resolveCreateConflict(paymentId, currentOperation)
            OperationType.UPDATE -> resolveUpdateConflict(paymentId, currentOperation)
            OperationType.DELETE -> resolveDeleteConflict(paymentId, currentOperation)
            else -> {
                Log.w("BulkCreditPaymentSync", "Unhandled conflict type for ${currentOperation.operationType}")
                false
            }
        }
    }

    private suspend fun resolveCreateConflict(paymentId: String, operation: PendingOperation): Boolean {
        try {
            localDataSource.updateSyncStatus(operation.entityId, SyncStatus.SYNCED)
            Log.w("BulkCreditPaymentSync", "CREATE conflict resolved for $paymentId - marked as synced")
            return true
        } catch (e: Exception) {
            Log.e("BulkCreditPaymentSync", "Failed to resolve CREATE conflict for $paymentId", e)
            return false
        }
    }

    private suspend fun resolveUpdateConflict(paymentId: String, operation: PendingOperation): Boolean {
        Log.w("BulkCreditPaymentSync", "UPDATE conflict for $paymentId - keeping as pending for retry")
        return false
    }

    private suspend fun resolveDeleteConflict(paymentId: String, operation: PendingOperation): Boolean {
        try {
            localDataSource.deleteLocalBulkCreditPaymentById(paymentId)
            Log.i("BulkCreditPaymentSync", "DELETE conflict resolved for $paymentId - removed locally")
            return true
        } catch (e: Exception) {
            Log.e("BulkCreditPaymentSync", "Failed to resolve DELETE conflict for $paymentId", e)
            return false
        }
    }

    private fun handleServiceUnavailable(
        paymentId: String,
        operation: String,
        currentOperation: PendingOperation
    ): Boolean {
        Log.w("BulkCreditPaymentSync", "Service unavailable for bulk credit payment $paymentId during $operation - retry later")
        return false
    }

    private fun handleServerError(
        paymentId: String,
        operation: String,
        currentOperation: PendingOperation,
        statusCode: Int? = null
    ): Boolean {
        val codeInfo = if (statusCode != null) " (code: $statusCode)" else ""
        Log.e("BulkCreditPaymentSync", "Server error for bulk credit payment $paymentId during $operation$codeInfo")
        return false
    }

    private fun handleBadRequest(
        paymentId: String,
        operation: String,
        currentOperation: PendingOperation
    ): Boolean {
        Log.e("BulkCreditPaymentSync", "Bad request for bulk credit payment $paymentId during $operation - check data format")
        return true
    }

    private fun handleUnauthorized(
        paymentId: String,
        operation: String,
        currentOperation: PendingOperation
    ): Boolean {
        Log.e("BulkCreditPaymentSync", "Unauthorized for bulk credit payment $paymentId during $operation - authentication required")
        return true
    }

    private fun handleForbidden(
        paymentId: String,
        operation: String,
        currentOperation: PendingOperation
    ): Boolean {
        Log.e("BulkCreditPaymentSync", "Forbidden for bulk credit payment $paymentId during $operation - insufficient permissions")
        return true
    }

    private fun handleBadGateway(
        paymentId: String,
        operation: String,
        currentOperation: PendingOperation
    ): Boolean {
        Log.w("BulkCreditPaymentSync", "Bad gateway for bulk credit payment $paymentId during $operation - retry later")
        return false
    }

    private fun handleGatewayTimeout(
        paymentId: String,
        operation: String,
        currentOperation: PendingOperation
    ): Boolean {
        Log.w("BulkCreditPaymentSync", "Gateway timeout for bulk credit payment $paymentId during $operation - retry later")
        return false
    }

    private fun handleClientError(
        paymentId: String,
        operation: String,
        statusCode: Int,
        currentOperation: PendingOperation
    ): Boolean {
        Log.e("BulkCreditPaymentSync", "Client error $statusCode for bulk credit payment $paymentId during $operation")
        return true
    }

    private fun handleGenericHttpError(
        paymentId: String,
        operation: String,
        statusCode: Int,
        currentOperation: PendingOperation
    ): Boolean {
        Log.e("BulkCreditPaymentSync", "HTTP error $statusCode for bulk credit payment $paymentId during $operation")
        return false
    }

    // --- Network and Generic Error Handlers ---
    private fun handleNetworkError(
        paymentId: String,
        operation: String,
        e: IOException,
        currentOperation: PendingOperation
    ): Boolean {
        Log.w("BulkCreditPaymentSync", "Network error for bulk credit payment $paymentId during $operation: ${e.message}")
        return false
    }

    private fun handleUnknownError(
        paymentId: String,
        operation: String,
        e: Exception,
        currentOperation: PendingOperation
    ): Boolean {
        Log.e("BulkCreditPaymentSync", "Unknown error for bulk credit payment $paymentId during $operation: ${e.message}", e)
        return false
    }

    // --- Push pending operations ---
    override suspend fun push(operations: List<PendingOperation>) {
        for (operation in operations) {
            syncOperation(operation, operations)
        }
    }

    override suspend fun hasCachedData(): Boolean = localDataSource.getLocalBulkCreditPaymentCount() != 0

    private suspend fun syncOperation(currentOperation: PendingOperation, allOperations: List<PendingOperation>) {
        val bulkCreditPayment = currentOperation.parsePayload<RemoteBulkCreditPayment>()
        val totalPending = allOperations.count { it.entityId == currentOperation.entityId }

        localDataSource.updateSyncStatus(currentOperation.entityId, SyncStatus.SYNCING)

        try {
            when (currentOperation.operationType) {
                OperationType.CREATE -> pushCreatedBulkCreditPayment(bulkCreditPayment, currentOperation)
                OperationType.UPDATE -> pushUpdatedBulkCreditPayment(bulkCreditPayment, currentOperation)
                OperationType.DELETE -> pushDeletedBulkCreditPayment(bulkCreditPayment, currentOperation)
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
            handleSyncFailure(currentOperation, e)
        }
    }

    private suspend fun handleSyncFailure(currentOperation: PendingOperation, e: Exception) {
        val isNotFoundError = e is HttpException && e.code() == HTTP_NOT_FOUND
        val isHandledError = when {
            isNotFoundError -> true
            e is HttpException && e.code() == HTTP_CONFLICT -> {
                when (currentOperation.operationType) {
                    OperationType.CREATE, OperationType.DELETE -> true
                    else -> false
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

            Log.e("BulkCreditPaymentSyncOperation", "Failed operation ${currentOperation.operationType} ${currentOperation.entityType} ${currentOperation.entityId}", e)
        }
    }

    private suspend fun updateFinalStatus(entityId: String) {
        val remaining = pendingOperationDao.countForEntity(entityId)
        val status = if (remaining > 0) SyncStatus.PENDING else SyncStatus.SYNCED
        localDataSource.updateSyncStatus(entityId, status)
    }

    override fun getEntity(): EntityType = EntityType.BulkCreditPayment

    // --- Sync Logic ---
    private fun shouldPerformFullSync(lastSync: Long?): Boolean {
        return when {
            lastSync == null -> true
            System.currentTimeMillis() - lastSync > SyncConfig.FULL_SYNC_THRESHOLD_MS -> true
            else -> false
        }
    }

    private suspend fun getLastSyncTimestamp(): Long? {
        return syncMetadataDao.getLastSyncTimestamp(EntityType.BulkCreditPayment)
    }

    private suspend fun updateLastSyncTimestamp(timestamp: Long, success: Boolean = true, error: String? = null) {
        syncMetadataDao.updateLastSync(EntityType.BulkCreditPayment, timestamp, success)
        if (error != null) {
            syncMetadataDao.updateSyncStatus(EntityType.BulkCreditPayment, false, error)
        }
    }

    private suspend fun processIncrementalChanges(organization: String, since: Long) {
        try {
            Log.i("BulkCreditPaymentSync", "Performing incremental sync since ${java.util.Date(since)}")

            val adjustedSince = since - (45 * 60 * 1000) // 45 minutes buffer

            val changes = remoteDataSource.getRemoteBulkCreditPaymentChangesSince(organization, adjustedSince)
            val remotePaymentIds = remoteDataSource.getRemoteBulkCreditPaymentIds(organization)

            val domainPayments = changes.map { bulkCreditPaymentMapper.mapRemoteToDomain(it) }
            val localEntities = domainPayments.map { bulkCreditPaymentMapper.mapDomainToLocal(it) }

            localEntities.forEach { entity ->
                localDataSource.insertLocalBulkCreditPayment(entity)
            }

            cleanupDeletedPayments(organization, remotePaymentIds)

            Log.i("BulkCreditPaymentSync", "Incremental sync completed: ${changes.size} updates")

        } catch (e: Exception) {
            Log.w("BulkCreditPaymentSync", "Incremental sync failed, will fall back to full sync", e)
            throw e
        }
    }

    private suspend fun processFullSync(organization: String) {
        Log.i("BulkCreditPaymentSync", "Performing full sync")
        val remotePayments = remoteDataSource.getRemoteBulkCreditPayments(organization)
        val remotePaymentIds = remoteDataSource.getRemoteBulkCreditPaymentIds(organization)

        val domainPayments = remotePayments.map { bulkCreditPaymentMapper.mapRemoteToDomain(it) }
        val localEntities = domainPayments.map { bulkCreditPaymentMapper.mapDomainToLocal(it) }

        localEntities.forEach {
            localDataSource.insertLocalBulkCreditPayment(it)
        }

        cleanupDeletedPayments(organization, remotePaymentIds)

        Log.i("BulkCreditPaymentSync", "Full sync completed: ${localEntities.size} bulk credit payments")
    }

    // --- Full pull from remote ---
    override suspend fun pullAll(organization: String) {
        Log.i("BulkCreditPaymentSync", "Sync started for organization: $organization")

        val lastSync = getLastSyncTimestamp()
        val shouldFullSync = shouldPerformFullSync(lastSync)

        Log.w("BulkCreditPaymentSync", "LastSync ${lastSync.toString()} should sync: $shouldFullSync")

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
                    Log.w("BulkCreditPaymentSync", "Incremental sync failed after $incrementalAttempts attempts, forcing full sync")
                    try {
                        processFullSync(organization)
                        success = true
                    } catch (fullSyncError: Exception) {
                        syncError = fullSyncError.message
                        Log.e("BulkCreditPaymentSync", "Full sync also failed", fullSyncError)
                    }
                } else {
                    Log.w("BulkCreditPaymentSync", "Incremental sync attempt $incrementalAttempts failed, retrying...", e)
                    delay(1000L * incrementalAttempts)
                }
            }
        }

        if (success) {
            updateLastSyncTimestamp(System.currentTimeMillis(), true)
            Log.i("BulkCreditPaymentSync", "Sync completed successfully")
        } else {
            updateLastSyncTimestamp(lastSync ?: 0L, false, syncError)
            Log.e("BulkCreditPaymentSync", "Sync failed after all attempts")
            throw RuntimeException("Sync failed: $syncError")
        }
    }

    private suspend fun cleanupDeletedPayments(
        organization: String,
        allRemoteIds: List<String>,
        maxDeletions: Int = 500
    ): CleanupResult {
        try {
            Log.d("BulkCreditPaymentSync", "Starting deletion cleanup for $organization...")

            val localIds = localDataSource.getLocalBulkCreditPaymentIds().toSet()
            val remoteIdsSet = allRemoteIds.toSet()
            val mustBeDeletedLocally = localIds.subtract(remoteIdsSet)

            return if (mustBeDeletedLocally.isNotEmpty()) {
                val idsToDelete = if (mustBeDeletedLocally.size > maxDeletions) {
                    Log.w("BulkCreditPaymentSync", "Too many deletions (${mustBeDeletedLocally.size}), limiting to $maxDeletions")
                    mustBeDeletedLocally.take(maxDeletions)
                } else {
                    mustBeDeletedLocally.toList()
                }

                Log.i("BulkCreditPaymentSync", "Found ${mustBeDeletedLocally.size} bulk credit payments to delete (processing ${idsToDelete.size})")

                idsToDelete.forEach { deletedId ->
                    localDataSource.deleteLocalBulkCreditPaymentById(deletedId)
                }

                CleanupResult(
                    success = true,
                    deletedCount = idsToDelete.size,
                    totalFound = mustBeDeletedLocally.size,
                    limited = mustBeDeletedLocally.size > maxDeletions
                ).also {
                    Log.i("BulkCreditPaymentSync", "Deletion cleanup completed: $it")
                }

            } else {
                CleanupResult(success = true, deletedCount = 0).also {
                    Log.d("BulkCreditPaymentSync", "No deleted bulk credit payments found")
                }
            }

        } catch (e: Exception) {
            Log.e("BulkCreditPaymentSync", "Failed during deletion cleanup", e)
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