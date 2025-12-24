package com.datavite.distrivite.data.sync.services

import android.util.Log
import com.datavite.distrivite.data.local.dao.PendingOperationDao
import com.datavite.distrivite.data.local.dao.SyncMetadataDao
import com.datavite.distrivite.data.local.datasource.CustomerLocalDataSource
import com.datavite.distrivite.data.local.model.PendingOperation
import com.datavite.distrivite.data.local.model.SyncStatus
import com.datavite.distrivite.data.mapper.CustomerMapper
import com.datavite.distrivite.data.remote.datasource.CustomerRemoteDataSource
import com.datavite.distrivite.data.remote.model.RemoteCustomer
import com.datavite.distrivite.data.sync.EntityType
import com.datavite.distrivite.data.sync.OperationScope
import com.datavite.distrivite.data.sync.OperationType
import com.datavite.distrivite.data.sync.SyncConfig
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException
import java.net.HttpURLConnection.*

import javax.inject.Inject

class CustomerSyncServiceImpl @Inject constructor(
    private val remoteDataSource: CustomerRemoteDataSource,
    private val localDataSource: CustomerLocalDataSource,
    private val customerMapper: CustomerMapper,
    private val syncMetadataDao: SyncMetadataDao,
    private val pendingOperationDao: PendingOperationDao,
) : CustomerSyncService {

    // --- Push CREATE ---
    private suspend fun pushCreatedCustomer(remoteCustomer: RemoteCustomer, currentOperation: PendingOperation) {
        try {
            remoteDataSource.createRemoteCustomer(remoteCustomer.orgSlug, remoteCustomer)
            val updatedDomain = customerMapper.mapRemoteToDomain(remoteCustomer)
            val localEntity = customerMapper.mapDomainToLocal(updatedDomain)

            localDataSource.insertLocalCustomer(localEntity)
            Log.i("CustomerSync", "Successfully synced created customer ${remoteCustomer.id}")
        } catch (e: Exception) {
            val handled = handleCustomerSyncException(e, remoteCustomer.id, currentOperation)
            if (!handled) throw e
        }
    }

    // --- Push UPDATE ---
    private suspend fun pushUpdatedCustomer(remoteCustomer: RemoteCustomer, currentOperation: PendingOperation) {
        try {
            remoteDataSource.updateRemoteCustomer(remoteCustomer.orgSlug, remoteCustomer)
            val updatedDomain = customerMapper.mapRemoteToDomain(remoteCustomer)
            val localEntity = customerMapper.mapDomainToLocal(updatedDomain)

            localDataSource.insertLocalCustomer(localEntity)
            Log.i("CustomerSync", "Successfully synced updated customer ${remoteCustomer.id}")
        } catch (e: Exception) {
            val handled = handleCustomerSyncException(e, remoteCustomer.id, currentOperation)
            if (!handled) throw e
        }
    }

    // --- Push DELETE ---
    private suspend fun pushDeletedCustomer(remoteCustomer: RemoteCustomer, currentOperation: PendingOperation) {
        try {
            remoteDataSource.deleteRemoteCustomer(remoteCustomer.orgSlug, remoteCustomer.id)
            Log.i("CustomerSync", "Successfully synced deleted customer ${remoteCustomer.id}")
        } catch (e: Exception) {
            val handled = handleCustomerSyncException(e, remoteCustomer.id, currentOperation)
            if (!handled) throw e
        }
    }

    // --- Comprehensive Exception Handling ---
    private suspend fun handleCustomerSyncException(e: Exception, customerId: String, currentOperation: PendingOperation): Boolean {
        return when (e) {
            is HttpException -> {
                when (e.code()) {
                    HTTP_NOT_FOUND -> handleNotFound(customerId, currentOperation)
                    HTTP_CONFLICT -> handleConflict(customerId, currentOperation)
                    HTTP_UNAVAILABLE -> handleServiceUnavailable(customerId, currentOperation)
                    HTTP_INTERNAL_ERROR -> handleServerError(customerId, currentOperation)
                    HTTP_BAD_REQUEST -> handleBadRequest(customerId, currentOperation)
                    HTTP_UNAUTHORIZED -> handleUnauthorized(customerId, currentOperation)
                    HTTP_FORBIDDEN -> handleForbidden(customerId, currentOperation)
                    HTTP_BAD_GATEWAY -> handleBadGateway(customerId, currentOperation)
                    HTTP_GATEWAY_TIMEOUT -> handleGatewayTimeout(customerId, currentOperation)
                    in 400..499 -> handleClientError(customerId, currentOperation, e.code())
                    in 500..599 -> handleServerError(customerId, currentOperation, e.code())
                    else -> handleGenericHttpError(customerId, currentOperation, e.code())
                }
            }
            is IOException -> handleNetworkError(customerId, currentOperation, e)
            else -> handleUnknownError(customerId, currentOperation, e)
        }
    }

    // --- HTTP Status Code Handlers ---
    private suspend fun handleNotFound(customerId: String, operation: PendingOperation): Boolean {
        Log.i("CustomerSync", "Customer $customerId not found during ${operation.operationType} - removing locally")
        return try {
            localDataSource.deleteLocalCustomerById(customerId)
            Log.i("CustomerSync", "Customer $customerId was deleted on server, removed locally")
            true  // Exception handled successfully
        } catch (e: Exception) {
            Log.e("CustomerSync", "Failed to clean up locally deleted customer $customerId", e)
            false  // Exception not fully handled
        }
    }

    private suspend fun handleConflict(customerId: String, operation: PendingOperation): Boolean {
        Log.w("CustomerSync", "Conflict detected for customer $customerId during ${operation.operationType} - resolving...")

        return when (operation.operationType) {
            OperationType.CREATE -> resolveCreateConflict(customerId, operation)
            OperationType.UPDATE -> resolveUpdateConflict(customerId, operation)
            OperationType.DELETE -> resolveDeleteConflict(customerId, operation)
            else -> {
                Log.w("CustomerSync", "Unhandled conflict type for ${operation.operationType}")
                false
            }
        }
    }

    private suspend fun resolveCreateConflict(customerId: String, operation: PendingOperation): Boolean {
        try {
            // For CREATE conflict, customer already exists on server
            localDataSource.updateSyncStatus(operation.entityId, SyncStatus.SYNCED)
            Log.w("CustomerSync", "CREATE conflict resolved for $customerId - marked as synced")
            return true
        } catch (e: Exception) {
            Log.e("CustomerSync", "Failed to resolve CREATE conflict for $customerId", e)
            return false
        }
    }

    private suspend fun resolveUpdateConflict(customerId: String, operation: PendingOperation): Boolean {
        Log.w("CustomerSync", "UPDATE conflict for $customerId - keeping as pending for retry")
        return false  // Not handled, will retry
    }

    private suspend fun resolveDeleteConflict(customerId: String, operation: PendingOperation): Boolean {
        try {
            // For DELETE conflict, item might already be deleted
            localDataSource.deleteLocalCustomerById(customerId)
            Log.i("CustomerSync", "DELETE conflict resolved for $customerId - removed locally")
            return true
        } catch (e: Exception) {
            Log.e("CustomerSync", "Failed to resolve DELETE conflict for $customerId", e)
            return false
        }
    }

    private fun handleServiceUnavailable(customerId: String, operation: PendingOperation): Boolean {
        Log.w("CustomerSync", "Service unavailable for customer $customerId during ${operation.operationType} - retry later")
        return false  // Not handled, will retry
    }

    private fun handleServerError(customerId: String, operation: PendingOperation, statusCode: Int? = null): Boolean {
        val codeInfo = if (statusCode != null) " (code: $statusCode)" else ""
        Log.e("CustomerSync", "Server error for customer $customerId during ${operation.operationType}$codeInfo")
        return false  // Not handled, will retry
    }

    private fun handleBadRequest(customerId: String, operation: PendingOperation): Boolean {
        Log.e("CustomerSync", "Bad request for customer $customerId during ${operation.operationType} - check data format")
        return true  // Handled - bad request won't succeed on retry
    }

    private fun handleUnauthorized(customerId: String, operation: PendingOperation): Boolean {
        Log.e("CustomerSync", "Unauthorized for customer $customerId during ${operation.operationType} - authentication required")
        return true  // Handled - auth error needs user intervention
    }

    private fun handleForbidden(customerId: String, operation: PendingOperation): Boolean {
        Log.e("CustomerSync", "Forbidden for customer $customerId during ${operation.operationType} - insufficient permissions")
        return true  // Handled - permission error won't succeed on retry
    }

    private fun handleBadGateway(customerId: String, operation: PendingOperation): Boolean {
        Log.w("CustomerSync", "Bad gateway for customer $customerId during ${operation.operationType} - retry later")
        return false  // Not handled, will retry
    }

    private fun handleGatewayTimeout(customerId: String, operation: PendingOperation): Boolean {
        Log.w("CustomerSync", "Gateway timeout for customer $customerId during ${operation.operationType} - retry later")
        return false  // Not handled, will retry
    }

    private fun handleClientError(customerId: String, operation: PendingOperation, statusCode: Int): Boolean {
        Log.e("CustomerSync", "Client error $statusCode for customer $customerId during ${operation.operationType}")
        return true  // Handled - client errors won't succeed on retry without fixing
    }

    private fun handleGenericHttpError(customerId: String, operation: PendingOperation, statusCode: Int): Boolean {
        Log.e("CustomerSync", "HTTP error $statusCode for customer $customerId during ${operation.operationType}")
        return false  // Not handled, generic case
    }

    // --- Network and Generic Error Handlers ---
    private fun handleNetworkError(customerId: String, operation: PendingOperation, e: IOException): Boolean {
        Log.w("CustomerSync", "Network error for customer $customerId during ${operation.operationType}: ${e.message}")
        return false  // Not handled, will retry
    }

    private fun handleUnknownError(customerId: String, operation: PendingOperation, e: Exception): Boolean {
        Log.e("CustomerSync", "Unknown error for customer $customerId during ${operation.operationType}: ${e.message}", e)
        return false  // Not handled
    }

    // --- Push pending operations ---
    override suspend fun push(operations: List<PendingOperation>) {
        for (operation in operations) {
            syncOperation(operation, operations)
        }
    }

    override suspend fun hasCachedData(): Boolean = localDataSource.getLocalCustomerCount() != 0

    private suspend fun syncOperation(currentOperation: PendingOperation, allOperations: List<PendingOperation>) {
        val customer = currentOperation.parsePayload<RemoteCustomer>()

        localDataSource.updateSyncStatus(currentOperation.entityId, SyncStatus.SYNCING)

        try {
            when (currentOperation.operationType) {
                OperationType.CREATE -> pushCreatedCustomer(customer, currentOperation)
                OperationType.UPDATE -> pushUpdatedCustomer(customer, currentOperation)
                OperationType.DELETE -> pushDeletedCustomer(customer, currentOperation)
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

            Log.e("CustomerSyncOperation", "Failed operation ${currentOperation.operationType} ${currentOperation.entityType} ${currentOperation.entityId}", e)
        }
    }

    override fun getEntity(): EntityType = EntityType.Customer

    // --- Sync Logic ---
    private fun shouldPerformFullSync(lastSync: Long?): Boolean {
        return when {
            lastSync == null -> true
            System.currentTimeMillis() - lastSync > SyncConfig.FULL_SYNC_THRESHOLD_MS -> true
            else -> false
        }
    }

    private suspend fun getLastSyncTimestamp(): Long? {
        return syncMetadataDao.getLastSyncTimestamp(EntityType.Customer)
    }

    private suspend fun updateLastSyncTimestamp(timestamp: Long, success: Boolean = true, error: String? = null) {
        syncMetadataDao.updateLastSync(EntityType.Customer, timestamp, success)
        if (error != null) {
            syncMetadataDao.updateSyncStatus(EntityType.Customer, false, error)
        }
    }

    private suspend fun processIncrementalChanges(organization: String, since: Long) {
        try {
            Log.i("CustomerSync", "Performing incremental sync since ${java.util.Date(since)}")

            val adjustedSince = since - (45 * 60 * 1000) // 45 minutes buffer

            val changes = remoteDataSource.getRemoteCustomersChangesSince(organization, adjustedSince)
            val remoteCustomerIds = remoteDataSource.getRemoteCustomerIds(organization)

            val domainCustomers = changes.map { customerMapper.mapRemoteToDomain(it) }
            val localEntities = domainCustomers.map { customerMapper.mapDomainToLocal(it) }

            localEntities.forEach { entity ->
                localDataSource.insertLocalCustomer(entity)
            }

            cleanupDeletedCustomers(organization, remoteCustomerIds)

            Log.i("CustomerSync", "Incremental sync completed: ${changes.size} updates")

        } catch (e: Exception) {
            Log.w("CustomerSync", "Incremental sync failed, will fall back to full sync", e)
            throw e
        }
    }

    private suspend fun processFullSync(organization: String) {
        Log.i("CustomerSync", "Performing full sync")
        val remoteCustomers = remoteDataSource.getRemoteCustomers(organization)
        val remoteCustomerIds = remoteDataSource.getRemoteCustomerIds(organization)

        val domainCustomers = remoteCustomers.map { customerMapper.mapRemoteToDomain(it) }
        val localEntities = domainCustomers.map { customerMapper.mapDomainToLocal(it) }

        localEntities.forEach {
            localDataSource.insertLocalCustomer(it)
        }

        cleanupDeletedCustomers(organization, remoteCustomerIds)

        Log.i("CustomerSync", "Full sync completed: ${localEntities.size} customers")
    }

    // --- Full pull from remote ---
    override suspend fun pullAll(organization: String) {
        Log.i("CustomerSync", "Sync started for organization: $organization")

        val lastSync = getLastSyncTimestamp()
        val shouldFullSync = shouldPerformFullSync(lastSync)

        Log.w("CustomerSync", "LastSync ${lastSync.toString()} should sync: $shouldFullSync")

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
                    Log.w("CustomerSync", "Incremental sync failed after $incrementalAttempts attempts, forcing full sync")
                    try {
                        processFullSync(organization)
                        success = true
                    } catch (fullSyncError: Exception) {
                        syncError = fullSyncError.message
                        Log.e("CustomerSync", "Full sync also failed", fullSyncError)
                    }
                } else {
                    Log.w("CustomerSync", "Incremental sync attempt $incrementalAttempts failed, retrying...", e)
                    delay(1000L * incrementalAttempts)
                }
            }
        }

        if (success) {
            updateLastSyncTimestamp(System.currentTimeMillis(), true)
            Log.i("CustomerSync", "Sync completed successfully")
        } else {
            updateLastSyncTimestamp(lastSync ?: 0L, false, syncError)
            Log.e("CustomerSync", "Sync failed after all attempts")
            throw RuntimeException("Sync failed: $syncError")
        }
    }

    private suspend fun cleanupDeletedCustomers(
        organization: String,
        allRemoteIds: List<String>,
        maxDeletions: Int = 500
    ): CleanupResult {
        try {
            Log.d("CustomerSync", "Starting deletion cleanup for $organization...")

            val localIds = localDataSource.getLocalCustomerIds().toSet()
            val remoteIdsSet = allRemoteIds.toSet()
            val mustBeDeletedLocally = localIds.subtract(remoteIdsSet)

            return if (mustBeDeletedLocally.isNotEmpty()) {
                val idsToDelete = if (mustBeDeletedLocally.size > maxDeletions) {
                    Log.w("CustomerSync", "Too many deletions (${mustBeDeletedLocally.size}), limiting to $maxDeletions")
                    mustBeDeletedLocally.take(maxDeletions)
                } else {
                    mustBeDeletedLocally.toList()
                }

                Log.i("CustomerSync", "Found ${mustBeDeletedLocally.size} customers to delete (processing ${idsToDelete.size})")

                idsToDelete.forEach { deletedId ->
                    localDataSource.deleteLocalCustomerById(deletedId)
                }

                CleanupResult(
                    success = true,
                    deletedCount = idsToDelete.size,
                    totalFound = mustBeDeletedLocally.size,
                    limited = mustBeDeletedLocally.size > maxDeletions
                ).also {
                    Log.i("CustomerSync", "Deletion cleanup completed: $it")
                }

            } else {
                CleanupResult(success = true, deletedCount = 0).also {
                    Log.d("CustomerSync", "No deleted customers found")
                }
            }

        } catch (e: Exception) {
            Log.e("CustomerSync", "Failed during deletion cleanup", e)
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