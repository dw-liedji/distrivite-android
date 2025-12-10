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
import com.datavite.distrivite.data.sync.OperationType
import com.datavite.distrivite.data.sync.SyncConfig
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException
import java.net.HttpURLConnection.HTTP_BAD_GATEWAY
import java.net.HttpURLConnection.HTTP_BAD_REQUEST
import java.net.HttpURLConnection.HTTP_CONFLICT
import java.net.HttpURLConnection.HTTP_FORBIDDEN
import java.net.HttpURLConnection.HTTP_GATEWAY_TIMEOUT
import java.net.HttpURLConnection.HTTP_INTERNAL_ERROR
import java.net.HttpURLConnection.HTTP_NOT_FOUND
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import java.net.HttpURLConnection.HTTP_UNAVAILABLE
import javax.inject.Inject

class BillingSyncServiceImpl @Inject constructor(
    private val remoteDataSource: BillingRemoteDataSource,
    private val localDataSource: BillingLocalDataSource,
    private val billingMapper: BillingMapper,
    private val syncMetadataDao: SyncMetadataDao,
    private val pendingOperationDao: PendingOperationDao,
) : BillingSyncService {

    // --- Push CREATE ---
    private suspend fun pushCreatedBilling(remoteBilling: RemoteBilling) {
        try {
            remoteDataSource.createRemoteBilling(remoteBilling.orgSlug, remoteBilling)
            val updatedDomain = billingMapper.mapRemoteToDomain(remoteBilling)
            val entities = billingMapper.mapDomainToLocalBillingWithItemsAndPaymentsRelation(updatedDomain)

            // Save parent + children
            localDataSource.insertLocalBillingWithItemsAndPaymentsRelation(entities)
            Log.i("BillingSync", "Successfully synced created billing ${remoteBilling.id}")
        } catch (e: Exception) {
            handleBillingSyncException(e, remoteBilling.id, "CREATE")
            throw e
        }
    }

    // --- Push UPDATE ---
    private suspend fun pushUpdatedBilling(remoteBilling: RemoteBilling) {
        try {
            remoteDataSource.updateRemoteBilling(remoteBilling.orgSlug, remoteBilling)
            val updatedDomain = billingMapper.mapRemoteToDomain(remoteBilling)
            val entities = billingMapper.mapDomainToLocalBillingWithItemsAndPaymentsRelation(updatedDomain)

            localDataSource.insertLocalBillingWithItemsAndPaymentsRelation(entities)
            Log.i("BillingSync", "Successfully synced updated billing ${remoteBilling.id}")
        } catch (e: Exception) {
            handleBillingSyncException(e, remoteBilling.id, "UPDATE")
            throw e
        }
    }

    private suspend fun pushDeliveredBilling(remoteBilling: RemoteBilling) {
        try {
            remoteDataSource.deliverRemoteBilling(remoteBilling.orgSlug, remoteBilling)
            val updatedDomain = billingMapper.mapRemoteToDomain(remoteBilling)
            val entities = billingMapper.mapDomainToLocalBillingWithItemsAndPaymentsRelation(updatedDomain)

            localDataSource.insertLocalBillingWithItemsAndPaymentsRelation(entities)
            Log.i("BillingSync", "Successfully synced updated billing ${remoteBilling.id}")
        } catch (e: Exception) {
            handleBillingSyncException(e, remoteBilling.id, "UPDATE")
            throw e
        }
    }

    // --- Push DELETE ---
    private suspend fun pushDeletedBilling(remoteBilling: RemoteBilling) {
        try {
            remoteDataSource.deleteRemoteBilling(remoteBilling.orgSlug, remoteBilling.id)
            Log.i("BillingSync", "Successfully synced deleted billing ${remoteBilling.id}")
        } catch (e: Exception) {
            handleBillingSyncException(e, remoteBilling.id, "DELETE")
            throw e
        }
    }

    // --- Comprehensive Exception Handling ---
    private suspend fun handleBillingSyncException(e: Exception, billingId: String, operation: String) {
        when (e) {
            is HttpException -> {
                when (e.code()) {
                    HTTP_NOT_FOUND -> handleNotFound(billingId, operation)
                    HTTP_CONFLICT -> handleConflict(billingId, operation)
                    HTTP_UNAVAILABLE -> handleServiceUnavailable(billingId, operation)
                    HTTP_INTERNAL_ERROR -> handleServerError(billingId, operation)
                    HTTP_BAD_REQUEST -> handleBadRequest(billingId, operation)
                    HTTP_UNAUTHORIZED -> handleUnauthorized(billingId, operation)
                    HTTP_FORBIDDEN -> handleForbidden(billingId, operation)
                    HTTP_BAD_GATEWAY -> handleBadGateway(billingId, operation)
                    HTTP_GATEWAY_TIMEOUT -> handleGatewayTimeout(billingId, operation)
                    in 400..499 -> handleClientError(billingId, operation, e.code())
                    in 500..599 -> handleServerError(billingId, operation, e.code())
                    else -> handleGenericHttpError(billingId, operation, e.code())
                }
            }
            is IOException -> handleNetworkError(billingId, operation, e)
            else -> handleUnknownError(billingId, operation, e)
        }
    }

    // --- HTTP Status Code Handlers ---
    private suspend fun handleNotFound(billingId: String, operation: String) {
        Log.i("BillingSync", "Billing $billingId not found during $operation - removing locally")
        // Object deleted on server - remove locally
        handleDeletedBilling(billingId, operation)
    }

    private fun handleConflict(billingId: String, operation: String) {
        Log.w("BillingSync", "Conflict detected for billing $billingId during $operation - requires resolution")
        // TODO: Implement conflict resolution logic
        // This could trigger a manual merge or use last-write-wins strategy
    }

    private fun handleServiceUnavailable(billingId: String, operation: String) {
        Log.w("BillingSync", "Service unavailable for billing $billingId during $operation - retry later")
        // Server is down - will retry on next sync cycle
    }

    private fun handleServerError(billingId: String, operation: String, statusCode: Int? = null) {
        val codeInfo = if (statusCode != null) " (code: $statusCode)" else ""
        Log.e("BillingSync", "Server error for billing $billingId during $operation$codeInfo")
        // Internal server error - retry with exponential backoff
    }

    private fun handleBadRequest(billingId: String, operation: String) {
        Log.e("BillingSync", "Bad request for billing $billingId during $operation - check data format")
        // Invalid request - likely data format issue
    }

    private fun handleUnauthorized(billingId: String, operation: String) {
        Log.e("BillingSync", "Unauthorized for billing $billingId during $operation - authentication required")
        // Token expired or invalid - trigger reauthentication
        // eventBus.post(AuthenticationRequiredEvent())
    }

    private fun handleForbidden(billingId: String, operation: String) {
        Log.e("BillingSync", "Forbidden for billing $billingId during $operation - insufficient permissions")
        // User doesn't have permission - should not retry
    }

    private fun handleBadGateway(billingId: String, operation: String) {
        Log.w("BillingSync", "Bad gateway for billing $billingId during $operation - retry later")
        // Proxy/gateway issue - retry with backoff
    }

    private fun handleGatewayTimeout(billingId: String, operation: String) {
        Log.w("BillingSync", "Gateway timeout for billing $billingId during $operation - retry later")
        // Gateway timeout - retry with backoff
    }

    private fun handleClientError(billingId: String, operation: String, statusCode: Int) {
        Log.e("BillingSync", "Client error $statusCode for billing $billingId during $operation")
        // Other 4xx errors - likely client-side issue
    }

    private fun handleGenericHttpError(billingId: String, operation: String, statusCode: Int) {
        Log.e("BillingSync", "HTTP error $statusCode for billing $billingId during $operation")
        // Unhandled HTTP status code
    }

    // --- Network and Generic Error Handlers ---
    private fun handleNetworkError(billingId: String, operation: String, e: IOException) {
        Log.w("BillingSync", "Network error for billing $billingId during $operation: ${e.message}")
        // Network connectivity issue - will retry when connection restored
    }

    private fun handleUnknownError(billingId: String, operation: String, e: Exception) {
        Log.e("BillingSync", "Unknown error for billing $billingId during $operation: ${e.message}", e)
        // Unexpected error - log for debugging
    }

    // --- Handle deleted billing (404 scenario) ---
    private suspend fun handleDeletedBilling(billingId: String, operationType: String) {
        try {
            // Remove from local database
            localDataSource.deleteLocalBillingById(billingId)

            // Log the cleanup action
            Log.i("BillingSync", "Billing $billingId was deleted on server during $operationType, removed locally")

            // You could also emit an event here for UI cleanup
            // eventBus.post(BillingDeletedEvent(billingId))

        } catch (e: Exception) {
            Log.e("BillingSync", "Failed to clean up locally deleted billing $billingId", e)
            // Don't throw - we want to consider the sync successful since the object is gone
        }
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
        val totalPending = allOperations.count { it.entityId == currentOperation.entityId }

        // Mark as syncing
        localDataSource.updateSyncStatus(currentOperation.entityId, SyncStatus.SYNCING)

        try {
            when (currentOperation.operationType) {
                OperationType.CREATE -> pushCreatedBilling(billing)
                OperationType.UPDATE -> pushUpdatedBilling(billing)
                OperationType.DELIVER_ORDER -> pushDeliveredBilling(billing)
                OperationType.DELETE -> pushDeletedBilling(billing)
                else -> {} // ignore other types
            }

            // Remove operation after success
            pendingOperationDao.deleteByKeys(
                entityType = currentOperation.entityType,
                entityId = currentOperation.entityId,
                operationType = currentOperation.operationType,
                orgId = currentOperation.orgId
            )

            // Update sync status depending on remaining operations
            val newStatus = if ((totalPending - 1) > 0) SyncStatus.PENDING else SyncStatus.SYNCED
            localDataSource.updateSyncStatus(currentOperation.entityId, newStatus)

        } catch (e: Exception) {
            // Don't increment failure count for 404 errors (they're handled successfully)
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

            // Only log as error if it's not a handled 404
            if (!isNotFoundError) {
                Log.e("BillingSyncOperation", "Failed operation ${currentOperation.operationType} ${currentOperation.entityType} ${currentOperation.entityId}", e)
            }
        }
    }

    override fun getEntity(): EntityType =
        EntityType.Billing


    // --- Sync Logic ---
    private fun shouldPerformFullSync(lastSync: Long?): Boolean {
        return when {
            lastSync == null -> true // First time sync
            System.currentTimeMillis() - lastSync > SyncConfig.FULL_SYNC_THRESHOLD_MS -> true // Too old
            else -> false // Use incremental
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

            // Add small buffer for clock skew
            val adjustedSince = since - (45 * 60 * 1000) // 45 minutes buffer

            // This assumes your backend supports incremental sync
            val changes = remoteDataSource.getRemoteBillingsChangesSince(organization, adjustedSince)
            val remoteBillingIds = remoteDataSource.getRemoteBillingIds(organization)

            val domainBillings = changes.map { billingMapper.mapRemoteToDomain(it) }
            val localEntities = domainBillings.map { billingMapper.mapDomainToLocalBillingWithItemsAndPaymentsRelation(it) }

            // Apply changes
            localEntities.forEach { entity ->
                localDataSource.insertLocalBillingWithItemsAndPaymentsRelation(entity)
            }

            // Remove deleted items
            cleanupDeletedBillings(organization, remoteBillingIds)

            Log.i("BillingSync", "Incremental sync completed: ${changes.size} updates")

        } catch (e: Exception) {
            Log.w("BillingSync", "Incremental sync failed, will fall back to full sync", e)
            throw e // Trigger fallback to full sync
        }
    }

    private suspend fun processFullSync(organization: String) {
        Log.i("BillingSync", "Performing full sync")
        val remoteBillings = remoteDataSource.getRemoteBillings(organization)
        val remoteBillingIds = remoteDataSource.getRemoteBillingIds(organization)

        val domainBillings = remoteBillings.map { billingMapper.mapRemoteToDomain(it) }
        val localEntities = domainBillings.map { billingMapper.mapDomainToLocalBillingWithItemsAndPaymentsRelation(it) }

        //  insert new
        localEntities.forEach {
            localDataSource.insertLocalBillingWithItemsAndPaymentsRelation(it)
        }

        // Clear existing data
        cleanupDeletedBillings(organization, remoteBillingIds)

        Log.i("BillingSync", "Full sync completed: ${localEntities.size} billings")
    }

    // --- Full pull from remote ---
    override suspend fun pullAll(organization: String) {
        Log.i("BillingSync", "Sync started for organization: $organization")

        //testSyncMetadataOperations()

        val lastSync = getLastSyncTimestamp()
        val shouldFullSync = shouldPerformFullSync(lastSync)

        Log.w("BillingSync", "LastSync ${lastSync.toString()} should sync: $shouldFullSync")

        var success = false
        var incrementalAttempts = 0
        var syncError: String? = null

        // Try incremental first, fall back to full sync if needed
        while (!success && incrementalAttempts <= SyncConfig.MAX_INCREMENTAL_RETRY_COUNT) {
            try {
                if (shouldFullSync || incrementalAttempts > 0) {
                    // Force full sync after incremental failures or if threshold passed
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
                    // Small delay before retry
                    delay(1000L * incrementalAttempts)
                }
            }
        }

        // Update sync timestamp
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
        allRemoteIds: List<String>, // Just pass IDs, not full objects
        maxDeletions: Int = 500
    ): CleanupResult {
        try {
            Log.d("BillingSync", "Starting deletion cleanup for $organization...")

            val localIds = localDataSource.getLocalBillingIds().toSet()
            val remoteIdsSet = allRemoteIds.toSet()

            // Items that should be removed locally because they no longer exist on the server
            val mustBeDeletedLocally = localIds.subtract(remoteIdsSet)

            return if (mustBeDeletedLocally.isNotEmpty()) {
                // Apply safety limit
                val idsToDelete = if (mustBeDeletedLocally.size > maxDeletions) {
                    Log.w("BillingSync", "Too many deletions (${mustBeDeletedLocally.size}), limiting to $maxDeletions")
                    mustBeDeletedLocally.take(maxDeletions)
                } else {
                    mustBeDeletedLocally.toList() // Convert back to list for consistent type
                }

                Log.i("BillingSync", "Found ${mustBeDeletedLocally.size} billings to delete (processing ${idsToDelete.size})")

                // Batch deletion for better performance
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