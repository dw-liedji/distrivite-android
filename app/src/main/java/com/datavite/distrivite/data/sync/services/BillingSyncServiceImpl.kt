package com.datavite.distrivite.data.sync.services

import android.util.Log
import com.datavite.distrivite.data.local.dao.PendingOperationDao
import com.datavite.distrivite.data.local.dao.SyncMetadataDao
import com.datavite.distrivite.data.local.datasource.BillingLocalDataSource
import com.datavite.distrivite.data.local.model.PendingOperation
import com.datavite.distrivite.data.local.model.SyncStatus
import com.datavite.distrivite.data.mapper.BillingMapper
import com.datavite.distrivite.data.remote.datasource.BillingRemoteDataSource
import com.datavite.distrivite.data.remote.model.RemoteBilling
import com.datavite.distrivite.data.sync.EntityType
import com.datavite.distrivite.data.sync.OperationScope
import com.datavite.distrivite.data.sync.OperationType
import com.datavite.distrivite.data.sync.SyncConfig
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException
import java.net.HttpURLConnection.*

import javax.inject.Inject

class BillingSyncServiceImpl @Inject constructor(
    private val remoteDataSource: BillingRemoteDataSource,
    private val localDataSource: BillingLocalDataSource,
    private val billingMapper: BillingMapper,
    private val syncMetadataDao: SyncMetadataDao,
    private val pendingOperationDao: PendingOperationDao,
) : BillingSyncService {

    // --- Push CREATE ---
    private suspend fun pushCreatedBilling(remoteBilling: RemoteBilling, currentOperation: PendingOperation) {
        try {
            remoteDataSource.createRemoteBilling(remoteBilling.orgSlug, remoteBilling)
            val updatedDomain = billingMapper.mapRemoteToDomain(remoteBilling)
            val entities = billingMapper.mapDomainToLocalBillingWithItemsAndPaymentsRelation(updatedDomain)

            localDataSource.insertLocalBillingWithItemsAndPaymentsRelation(entities)
            Log.i("BillingSync", "Successfully synced created billing ${remoteBilling.id}")
        } catch (e: Exception) {
            val handled = handleBillingSyncException(e, remoteBilling.id, currentOperation)
            if (!handled) throw e
        }
    }

    // --- Push UPDATE ---
    private suspend fun pushUpdatedBilling(remoteBilling: RemoteBilling, currentOperation: PendingOperation) {
        try {
            remoteDataSource.updateRemoteBilling(remoteBilling.orgSlug, remoteBilling)
            val updatedDomain = billingMapper.mapRemoteToDomain(remoteBilling)
            val entities = billingMapper.mapDomainToLocalBillingWithItemsAndPaymentsRelation(updatedDomain)

            localDataSource.insertLocalBillingWithItemsAndPaymentsRelation(entities)
            Log.i("BillingSync", "Successfully synced updated billing ${remoteBilling.id}")
        } catch (e: Exception) {
            val handled = handleBillingSyncException(e, remoteBilling.id, currentOperation)
            if (!handled) throw e
        }
    }

    private suspend fun pushDeliveredBilling(remoteBilling: RemoteBilling, currentOperation: PendingOperation) {
        try {
            remoteDataSource.deliverRemoteBilling(remoteBilling.orgSlug, remoteBilling)
            val updatedDomain = billingMapper.mapRemoteToDomain(remoteBilling)
            val entities = billingMapper.mapDomainToLocalBillingWithItemsAndPaymentsRelation(updatedDomain)

            localDataSource.insertLocalBillingWithItemsAndPaymentsRelation(entities)
            Log.i("BillingSync", "Successfully delivered billing ${remoteBilling.id}")
        } catch (e: Exception) {
            val handled = handleBillingSyncException(e, remoteBilling.id, currentOperation)
            if (!handled) throw e
        }
    }

    // --- Push DELETE ---
    private suspend fun pushDeletedBilling(remoteBilling: RemoteBilling, currentOperation: PendingOperation) {
        try {
            remoteDataSource.deleteRemoteBilling(remoteBilling.orgSlug, remoteBilling.id)
            Log.i("BillingSync", "Successfully synced deleted billing ${remoteBilling.id}")
        } catch (e: Exception) {
            val handled = handleBillingSyncException(e, remoteBilling.id, currentOperation)
            if (!handled) throw e
        }
    }

    // --- Comprehensive Exception Handling ---
    private suspend fun handleBillingSyncException(e: Exception, billingId: String, currentOperation: PendingOperation): Boolean {
        return when (e) {
            is HttpException -> {
                when (e.code()) {
                    HTTP_NOT_FOUND -> handleNotFound(billingId, currentOperation)
                    HTTP_CONFLICT -> handleConflict(billingId, currentOperation)
                    HTTP_UNAVAILABLE -> handleServiceUnavailable(billingId, currentOperation)
                    HTTP_INTERNAL_ERROR -> handleServerError(billingId, currentOperation)
                    HTTP_BAD_REQUEST -> handleBadRequest(billingId, currentOperation)
                    HTTP_UNAUTHORIZED -> handleUnauthorized(billingId, currentOperation)
                    HTTP_FORBIDDEN -> handleForbidden(billingId, currentOperation)
                    HTTP_BAD_GATEWAY -> handleBadGateway(billingId, currentOperation)
                    HTTP_GATEWAY_TIMEOUT -> handleGatewayTimeout(billingId, currentOperation)
                    in 400..499 -> handleClientError(billingId, currentOperation, e.code())
                    in 500..599 -> handleServerError(billingId, currentOperation, e.code())
                    else -> handleGenericHttpError(billingId, currentOperation, e.code())
                }
            }
            is IOException -> handleNetworkError(billingId, currentOperation, e)
            else -> handleUnknownError(billingId, currentOperation, e)
        }
    }

    // --- HTTP Status Code Handlers ---
    private suspend fun handleNotFound(billingId: String, operation: PendingOperation): Boolean {
        Log.i("BillingSync", "Billing $billingId not found during ${operation.operationType} - removing locally")
        return try {
            localDataSource.deleteLocalBillingById(billingId)
            Log.i("BillingSync", "Billing $billingId was deleted on server, removed locally")
            true  // Exception handled successfully
        } catch (e: Exception) {
            Log.e("BillingSync", "Failed to clean up locally deleted billing $billingId", e)
            false  // Exception not fully handled
        }
    }

    private suspend fun handleConflict(billingId: String, operation: PendingOperation): Boolean {
        Log.w("BillingSync", "Conflict detected for billing $billingId during ${operation.operationType} - resolving...")

        return when (operation.operationType) {
            OperationType.CREATE -> resolveCreateConflict(billingId, operation)
            OperationType.UPDATE -> resolveUpdateConflict(billingId, operation)
            OperationType.DELIVER_ORDER -> resolveDeliverConflict(billingId, operation)
            OperationType.DELETE -> resolveDeleteConflict(billingId, operation)
            else -> {
                Log.w("BillingSync", "Unhandled conflict type for ${operation.operationType}")
                false
            }
        }
    }

    private suspend fun resolveCreateConflict(billingId: String, operation: PendingOperation): Boolean {
        try {
            // For CREATE conflict, billing already exists on server
            localDataSource.updateSyncStatus(operation.entityId, SyncStatus.SYNCED)
            Log.w("BillingSync", "CREATE conflict resolved for $billingId - marked as synced")
            return true
        } catch (e: Exception) {
            Log.e("BillingSync", "Failed to resolve CREATE conflict for $billingId", e)
            return false
        }
    }

    private suspend fun resolveUpdateConflict(billingId: String, operation: PendingOperation): Boolean {
        Log.w("BillingSync", "UPDATE conflict for $billingId - keeping as pending for retry")
        return false  // Not handled, will retry
    }

    private suspend fun resolveDeliverConflict(billingId: String, operation: PendingOperation): Boolean {
        Log.w("BillingSync", "DELIVER_ORDER conflict for $billingId - keeping as pending for retry")
        return false  // Not handled, will retry
    }

    private suspend fun resolveDeleteConflict(billingId: String, operation: PendingOperation): Boolean {
        try {
            // For DELETE conflict, item might already be deleted
            localDataSource.deleteLocalBillingById(billingId)
            Log.i("BillingSync", "DELETE conflict resolved for $billingId - removed locally")
            return true
        } catch (e: Exception) {
            Log.e("BillingSync", "Failed to resolve DELETE conflict for $billingId", e)
            return false
        }
    }

    private fun handleServiceUnavailable(billingId: String, operation: PendingOperation): Boolean {
        Log.w("BillingSync", "Service unavailable for billing $billingId during ${operation.operationType} - retry later")
        return false  // Not handled, will retry
    }

    private fun handleServerError(billingId: String, operation: PendingOperation, statusCode: Int? = null): Boolean {
        val codeInfo = if (statusCode != null) " (code: $statusCode)" else ""
        Log.e("BillingSync", "Server error for billing $billingId during ${operation.operationType}$codeInfo")
        return false  // Not handled, will retry
    }

    private fun handleBadRequest(billingId: String, operation: PendingOperation): Boolean {
        Log.e("BillingSync", "Bad request for billing $billingId during ${operation.operationType} - check data format")
        return true  // Handled - bad request won't succeed on retry
    }

    private fun handleUnauthorized(billingId: String, operation: PendingOperation): Boolean {
        Log.e("BillingSync", "Unauthorized for billing $billingId during ${operation.operationType} - authentication required")
        return true  // Handled - auth error needs user intervention
    }

    private fun handleForbidden(billingId: String, operation: PendingOperation): Boolean {
        Log.e("BillingSync", "Forbidden for billing $billingId during ${operation.operationType} - insufficient permissions")
        return true  // Handled - permission error won't succeed on retry
    }

    private fun handleBadGateway(billingId: String, operation: PendingOperation): Boolean {
        Log.w("BillingSync", "Bad gateway for billing $billingId during ${operation.operationType} - retry later")
        return false  // Not handled, will retry
    }

    private fun handleGatewayTimeout(billingId: String, operation: PendingOperation): Boolean {
        Log.w("BillingSync", "Gateway timeout for billing $billingId during ${operation.operationType} - retry later")
        return false  // Not handled, will retry
    }

    private fun handleClientError(billingId: String, operation: PendingOperation, statusCode: Int): Boolean {
        Log.e("BillingSync", "Client error $statusCode for billing $billingId during ${operation.operationType}")
        return true  // Handled - client errors won't succeed on retry without fixing
    }

    private fun handleGenericHttpError(billingId: String, operation: PendingOperation, statusCode: Int): Boolean {
        Log.e("BillingSync", "HTTP error $statusCode for billing $billingId during ${operation.operationType}")
        return false  // Not handled, generic case
    }

    // --- Network and Generic Error Handlers ---
    private fun handleNetworkError(billingId: String, operation: PendingOperation, e: IOException): Boolean {
        Log.w("BillingSync", "Network error for billing $billingId during ${operation.operationType}: ${e.message}")
        return false  // Not handled, will retry
    }

    private fun handleUnknownError(billingId: String, operation: PendingOperation, e: Exception): Boolean {
        Log.e("BillingSync", "Unknown error for billing $billingId during ${operation.operationType}: ${e.message}", e)
        return false  // Not handled
    }

    // --- Push pending operations ---
    override suspend fun push(operations: List<PendingOperation>) {
        for (operation in operations) {
            syncOperation(operation, operations)
        }
    }

    override suspend fun hasCachedData(): Boolean = localDataSource.getLocalBillingCount() != 0

    private suspend fun syncOperation(currentOperation: PendingOperation, allOperations: List<PendingOperation>) {
        val billing = currentOperation.parsePayload<RemoteBilling>()

        localDataSource.updateSyncStatus(currentOperation.entityId, SyncStatus.SYNCING)

        try {
            when (currentOperation.operationType) {
                OperationType.CREATE -> pushCreatedBilling(billing, currentOperation)
                OperationType.UPDATE -> pushUpdatedBilling(billing, currentOperation)
                OperationType.DELIVER_ORDER -> pushDeliveredBilling(billing, currentOperation)
                OperationType.DELETE -> pushDeletedBilling(billing, currentOperation)
                else -> {} // ignore other types
            }

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
                    OperationType.CREATE, OperationType.DELETE, OperationType.DELIVER_ORDER -> true
                    else -> false  // UPDATE and other conflicts not handled
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

            Log.e("BillingSyncOperation", "Failed operation ${currentOperation.operationType} ${currentOperation.entityType} ${currentOperation.entityId}", e)
        }
    }

    override fun getEntity(): EntityType = EntityType.Billing

    // --- Sync Logic ---
    private fun shouldPerformFullSync(lastSync: Long?): Boolean {
        return when {
            lastSync == null -> true
            System.currentTimeMillis() - lastSync > SyncConfig.FULL_SYNC_THRESHOLD_MS -> true
            else -> false
        }
    }

    private suspend fun getLastSyncTimestamp(): Long? {
        return syncMetadataDao.getLastSyncTimestamp(EntityType.Billing)
    }

    private suspend fun updateLastSyncTimestamp(timestamp: Long, success: Boolean = true, error: String? = null) {
        syncMetadataDao.updateLastSync(EntityType.Billing, timestamp, success)
        if (error != null) {
            syncMetadataDao.updateSyncStatus(EntityType.Billing, false, error)
        }
    }

    private suspend fun processIncrementalChanges(organization: String, since: Long) {
        try {
            Log.i("BillingSync", "Performing incremental sync since ${java.util.Date(since)}")

            val adjustedSince = since - (45 * 60 * 1000) // 45 minutes buffer

            val changes = remoteDataSource.getRemoteBillingsChangesSince(organization, adjustedSince)
            val remoteBillingIds = remoteDataSource.getRemoteBillingIds(organization)

            val domainBillings = changes.map { billingMapper.mapRemoteToDomain(it) }
            val localEntities = domainBillings.map { billingMapper.mapDomainToLocalBillingWithItemsAndPaymentsRelation(it) }

            localEntities.forEach { entity ->
                localDataSource.insertLocalBillingWithItemsAndPaymentsRelation(entity)
            }

            cleanupDeletedBillings(organization, remoteBillingIds)

            Log.i("BillingSync", "Incremental sync completed: ${changes.size} updates")

        } catch (e: Exception) {
            Log.w("BillingSync", "Incremental sync failed, will fall back to full sync", e)
            throw e
        }
    }

    private suspend fun processFullSync(organization: String) {
        Log.i("BillingSync", "Performing full sync")
        val remoteBillings = remoteDataSource.getRemoteBillings(organization)
        val remoteBillingIds = remoteDataSource.getRemoteBillingIds(organization)

        val domainBillings = remoteBillings.map { billingMapper.mapRemoteToDomain(it) }
        val localEntities = domainBillings.map { billingMapper.mapDomainToLocalBillingWithItemsAndPaymentsRelation(it) }

        localEntities.forEach {
            localDataSource.insertLocalBillingWithItemsAndPaymentsRelation(it)
        }

        cleanupDeletedBillings(organization, remoteBillingIds)

        Log.i("BillingSync", "Full sync completed: ${localEntities.size} billings")
    }

    // --- Full pull from remote ---
    override suspend fun pullAll(organization: String) {
        Log.i("BillingSync", "Sync started for organization: $organization")

        val lastSync = getLastSyncTimestamp()
        val shouldFullSync = shouldPerformFullSync(lastSync)

        Log.w("BillingSync", "LastSync ${lastSync.toString()} should sync: $shouldFullSync")

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
                    Log.w("BillingSync", "Incremental sync failed after $incrementalAttempts attempts, forcing full sync")
                    try {
                        processFullSync(organization)
                        success = true
                    } catch (fullSyncError: Exception) {
                        syncError = fullSyncError.message
                        Log.e("BillingSync", "Full sync also failed", fullSyncError)
                    }
                } else {
                    Log.w("BillingSync", "Incremental sync attempt $incrementalAttempts failed, retrying...", e)
                    delay(1000L * incrementalAttempts)
                }
            }
        }

        if (success) {
            updateLastSyncTimestamp(System.currentTimeMillis(), true)
            Log.i("BillingSync", "Sync completed successfully")
        } else {
            updateLastSyncTimestamp(lastSync ?: 0L, false, syncError)
            Log.e("BillingSync", "Sync failed after all attempts")
            throw RuntimeException("Sync failed: $syncError")
        }
    }

    private suspend fun cleanupDeletedBillings(
        organization: String,
        allRemoteIds: List<String>,
        maxDeletions: Int = 500
    ): CleanupResult {
        try {
            Log.d("BillingSync", "Starting deletion cleanup for $organization...")

            val localIds = localDataSource.getLocalBillingIds().toSet()
            val remoteIdsSet = allRemoteIds.toSet()
            val mustBeDeletedLocally = localIds.subtract(remoteIdsSet)

            return if (mustBeDeletedLocally.isNotEmpty()) {
                val idsToDelete = if (mustBeDeletedLocally.size > maxDeletions) {
                    Log.w("BillingSync", "Too many deletions (${mustBeDeletedLocally.size}), limiting to $maxDeletions")
                    mustBeDeletedLocally.take(maxDeletions)
                } else {
                    mustBeDeletedLocally.toList()
                }

                Log.i("BillingSync", "Found ${mustBeDeletedLocally.size} billings to delete (processing ${idsToDelete.size})")

                idsToDelete.forEach { deletedId ->
                    localDataSource.deleteLocalBillingById(deletedId)
                }

                CleanupResult(
                    success = true,
                    deletedCount = idsToDelete.size,
                    totalFound = mustBeDeletedLocally.size,
                    limited = mustBeDeletedLocally.size > maxDeletions
                ).also {
                    Log.i("BillingSync", "Deletion cleanup completed: $it")
                }

            } else {
                CleanupResult(success = true, deletedCount = 0).also {
                    Log.d("BillingSync", "No deleted billings found")
                }
            }

        } catch (e: Exception) {
            Log.e("BillingSync", "Failed during deletion cleanup", e)
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