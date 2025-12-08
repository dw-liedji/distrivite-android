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
    private suspend fun pushCreatedCustomer(remoteCustomer: RemoteCustomer) {
        try {
            remoteDataSource.createRemoteCustomer(remoteCustomer.orgSlug, remoteCustomer)
            val updatedDomain = customerMapper.mapRemoteToDomain(remoteCustomer)
            val localEntity = customerMapper.mapDomainToLocal(updatedDomain)

            localDataSource.insertLocalCustomer(localEntity)
            Log.i("CustomerSync", "Successfully synced created customer ${remoteCustomer.id}")
        } catch (e: Exception) {
            handleCustomerSyncException(e, remoteCustomer.id, "CREATE")
            throw e
        }
    }

    // --- Push UPDATE ---
    private suspend fun pushUpdatedCustomer(remoteCustomer: RemoteCustomer) {
        try {
            remoteDataSource.updateRemoteCustomer(remoteCustomer.orgSlug, remoteCustomer)
            val updatedDomain = customerMapper.mapRemoteToDomain(remoteCustomer)
            val localEntity = customerMapper.mapDomainToLocal(updatedDomain)

            localDataSource.insertLocalCustomer(localEntity)
            Log.i("CustomerSync", "Successfully synced updated customer ${remoteCustomer.id}")
        } catch (e: Exception) {
            handleCustomerSyncException(e, remoteCustomer.id, "UPDATE")
            throw e
        }
    }

    // --- Push DELETE ---
    private suspend fun pushDeletedCustomer(remoteCustomer: RemoteCustomer) {
        try {
            remoteDataSource.deleteRemoteCustomer(remoteCustomer.orgSlug, remoteCustomer.id)
            Log.i("CustomerSync", "Successfully synced deleted customer ${remoteCustomer.id}")
        } catch (e: Exception) {
            handleCustomerSyncException(e, remoteCustomer.id, "DELETE")
            throw e
        }
    }

    // --- Comprehensive Exception Handling ---
    private suspend fun handleCustomerSyncException(e: Exception, customerId: String, operation: String) {
        when (e) {
            is HttpException -> {
                when (e.code()) {
                    HTTP_NOT_FOUND -> handleNotFound(customerId, operation)
                    HTTP_CONFLICT -> handleConflict(customerId, operation)
                    HTTP_UNAVAILABLE -> handleServiceUnavailable(customerId, operation)
                    HTTP_INTERNAL_ERROR -> handleServerError(customerId, operation)
                    HTTP_BAD_REQUEST -> handleBadRequest(customerId, operation)
                    HTTP_UNAUTHORIZED -> handleUnauthorized(customerId, operation)
                    HTTP_FORBIDDEN -> handleForbidden(customerId, operation)
                    HTTP_BAD_GATEWAY -> handleBadGateway(customerId, operation)
                    HTTP_GATEWAY_TIMEOUT -> handleGatewayTimeout(customerId, operation)
                    in 400..499 -> handleClientError(customerId, operation, e.code())
                    in 500..599 -> handleServerError(customerId, operation, e.code())
                    else -> handleGenericHttpError(customerId, operation, e.code())
                }
            }
            is IOException -> handleNetworkError(customerId, operation, e)
            else -> handleUnknownError(customerId, operation, e)
        }
    }

    // --- HTTP Status Code Handlers ---
    private suspend fun handleNotFound(customerId: String, operation: String) {
        Log.i("CustomerSync", "Customer $customerId not found during $operation - removing locally")
        handleDeletedCustomer(customerId, operation)
    }

    private fun handleConflict(customerId: String, operation: String) {
        Log.w("CustomerSync", "Conflict detected for customer $customerId during $operation - requires resolution")
    }

    private fun handleServiceUnavailable(customerId: String, operation: String) {
        Log.w("CustomerSync", "Service unavailable for customer $customerId during $operation - retry later")
    }

    private fun handleServerError(customerId: String, operation: String, statusCode: Int? = null) {
        val codeInfo = if (statusCode != null) " (code: $statusCode)" else ""
        Log.e("CustomerSync", "Server error for customer $customerId during $operation$codeInfo")
    }

    private fun handleBadRequest(customerId: String, operation: String) {
        Log.e("CustomerSync", "Bad request for customer $customerId during $operation - check data format")
    }

    private fun handleUnauthorized(customerId: String, operation: String) {
        Log.e("CustomerSync", "Unauthorized for customer $customerId during $operation - authentication required")
    }

    private fun handleForbidden(customerId: String, operation: String) {
        Log.e("CustomerSync", "Forbidden for customer $customerId during $operation - insufficient permissions")
    }

    private fun handleBadGateway(customerId: String, operation: String) {
        Log.w("CustomerSync", "Bad gateway for customer $customerId during $operation - retry later")
    }

    private fun handleGatewayTimeout(customerId: String, operation: String) {
        Log.w("CustomerSync", "Gateway timeout for customer $customerId during $operation - retry later")
    }

    private fun handleClientError(customerId: String, operation: String, statusCode: Int) {
        Log.e("CustomerSync", "Client error $statusCode for customer $customerId during $operation")
    }

    private fun handleGenericHttpError(customerId: String, operation: String, statusCode: Int) {
        Log.e("CustomerSync", "HTTP error $statusCode for customer $customerId during $operation")
    }

    // --- Network and Generic Error Handlers ---
    private fun handleNetworkError(customerId: String, operation: String, e: IOException) {
        Log.w("CustomerSync", "Network error for customer $customerId during $operation: ${e.message}")
    }

    private fun handleUnknownError(customerId: String, operation: String, e: Exception) {
        Log.e("CustomerSync", "Unknown error for customer $customerId during $operation: ${e.message}", e)
    }

    // --- Handle deleted customer (404 scenario) ---
    private suspend fun handleDeletedCustomer(customerId: String, operationType: String) {
        try {
            localDataSource.deleteLocalCustomerById(customerId)
            Log.i("CustomerSync", "Customer $customerId was deleted on server during $operationType, removed locally")
        } catch (e: Exception) {
            Log.e("CustomerSync", "Failed to clean up locally deleted customer $customerId", e)
        }
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
        val totalPending = allOperations.count { it.entityId == currentOperation.entityId }

        localDataSource.updateSyncStatus(currentOperation.entityId, SyncStatus.SYNCING)

        try {
            when (currentOperation.operationType) {
                OperationType.CREATE -> pushCreatedCustomer(customer)
                OperationType.UPDATE -> pushUpdatedCustomer(customer)
                OperationType.DELETE -> pushDeletedCustomer(customer)
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
                Log.e("CustomerSyncOperation", "Failed operation ${currentOperation.operationType} ${currentOperation.entityType} ${currentOperation.entityId}", e)
            }
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