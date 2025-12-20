package com.datavite.distrivite.data.sync.services

import android.util.Log
import com.datavite.distrivite.data.local.dao.PendingOperationDao
import com.datavite.distrivite.data.local.dao.SyncMetadataDao
import com.datavite.distrivite.data.local.datasource.StockLocalDataSource
import com.datavite.distrivite.data.local.model.PendingOperation
import com.datavite.distrivite.data.local.model.SyncStatus
import com.datavite.distrivite.data.mapper.StockMapper
import com.datavite.distrivite.data.remote.datasource.StockRemoteDataSource
import com.datavite.distrivite.data.remote.model.RemoteStock
import com.datavite.distrivite.data.sync.EntityType
import com.datavite.distrivite.data.sync.OperationType
import com.datavite.distrivite.data.sync.SyncConfig
import com.datavite.distrivite.domain.notification.NotificationEvent
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException
import java.net.HttpURLConnection.*
import javax.inject.Inject

class StockSyncServiceImpl @Inject constructor(
    private val remoteDataSource: StockRemoteDataSource,
    private val localDataSource: StockLocalDataSource,
    private val stockMapper: StockMapper,
    private val syncMetadataDao: SyncMetadataDao,
    private val pendingOperationDao: PendingOperationDao,
) : StockSyncService {

    // --- Push CREATE ---
    private suspend fun pushCreatedStock(remoteStock: RemoteStock) {
        try {
            remoteDataSource.createRemoteStock(remoteStock.orgSlug, remoteStock)
            val updatedDomain = stockMapper.mapRemoteToDomain(remoteStock)
            val localEntity = stockMapper.mapDomainToLocal(updatedDomain)

            localDataSource.insertLocalStock(localEntity)
            Log.i("StockSync", "Successfully synced created stock ${remoteStock.id}")
        } catch (e: Exception) {
            handleStockSyncException(e, remoteStock.id, "CREATE")
            throw e
        }
    }

    // --- Push UPDATE ---
    private suspend fun pushUpdatedStock(remoteStock: RemoteStock) {
        try {
            remoteDataSource.updateRemoteStock(remoteStock.orgSlug, remoteStock)
            val updatedDomain = stockMapper.mapRemoteToDomain(remoteStock)
            val localEntity = stockMapper.mapDomainToLocal(updatedDomain)

            localDataSource.insertLocalStock(localEntity)
            Log.i("StockSync", "Successfully synced updated stock ${remoteStock.id}")
        } catch (e: Exception) {
            handleStockSyncException(e, remoteStock.id, "UPDATE")
            throw e
        }
    }

    // --- Push UPDATE QUANTITY ---
    private suspend fun pushUpdatedStockQuantity(remoteStock: RemoteStock) {
        try {
            Log.i("StockSync", "synced updated stock quantity ${remoteStock.id} started")

            val remote = remoteDataSource.updateRemoteStockQuantity(remoteStock.orgSlug, remoteStock)
            val updatedDomain = stockMapper.mapRemoteToDomain(remote)
            val localEntity = stockMapper.mapDomainToLocal(updatedDomain)

            localDataSource.insertLocalStock(localEntity)
            Log.i("StockSync", "Successfully synced updated stock quantity ${remoteStock.id}")
        } catch (e: Exception) {
            handleStockSyncException(e, remoteStock.id, "UPDATE")
            throw e
        }
    }


    // --- Push DELETE ---
    private suspend fun pushDeletedStock(remoteStock: RemoteStock) {
        try {
            remoteDataSource.deleteRemoteStock(remoteStock.orgSlug, remoteStock.id)
            Log.i("StockSync", "Successfully synced deleted stock ${remoteStock.id}")
        } catch (e: Exception) {
            handleStockSyncException(e, remoteStock.id, "DELETE")
            throw e
        }
    }

    // --- Comprehensive Exception Handling ---
    private suspend fun handleStockSyncException(e: Exception, stockId: String, operation: String) {
        when (e) {
            is HttpException -> {
                when (e.code()) {
                    HTTP_NOT_FOUND -> handleNotFound(stockId, operation)
                    HTTP_CONFLICT -> handleConflict(stockId, operation)
                    HTTP_UNAVAILABLE -> handleServiceUnavailable(stockId, operation)
                    HTTP_INTERNAL_ERROR -> handleServerError(stockId, operation)
                    HTTP_BAD_REQUEST -> handleBadRequest(stockId, operation)
                    HTTP_UNAUTHORIZED -> handleUnauthorized(stockId, operation)
                    HTTP_FORBIDDEN -> handleForbidden(stockId, operation)
                    HTTP_BAD_GATEWAY -> handleBadGateway(stockId, operation)
                    HTTP_GATEWAY_TIMEOUT -> handleGatewayTimeout(stockId, operation)
                    in 400..499 -> handleClientError(stockId, operation, e.code())
                    in 500..599 -> handleServerError(stockId, operation, e.code())
                    else -> handleGenericHttpError(stockId, operation, e.code())
                }
            }
            is IOException -> handleNetworkError(stockId, operation, e)
            else -> handleUnknownError(stockId, operation, e)
        }
    }

    // --- HTTP Status Code Handlers ---
    private suspend fun handleNotFound(stockId: String, operation: String) {
        Log.i("StockSync", "Stock $stockId not found during $operation - removing locally")
        handleDeletedStock(stockId, operation)
    }

    private fun handleConflict(stockId: String, operation: String) {
        Log.w("StockSync", "Conflict detected for stock $stockId during $operation - requires resolution")
    }

    private fun handleServiceUnavailable(stockId: String, operation: String) {
        Log.w("StockSync", "Service unavailable for stock $stockId during $operation - retry later")
    }

    private fun handleServerError(stockId: String, operation: String, statusCode: Int? = null) {
        val codeInfo = if (statusCode != null) " (code: $statusCode)" else ""
        Log.e("StockSync", "Server error for stock $stockId during $operation$codeInfo")
    }

    private fun handleBadRequest(stockId: String, operation: String) {
        Log.e("StockSync", "Bad request for stock $stockId during $operation - check data format")
    }

    private fun handleUnauthorized(stockId: String, operation: String) {
        Log.e("StockSync", "Unauthorized for stock $stockId during $operation - authentication required")
    }

    private fun handleForbidden(stockId: String, operation: String) {
        Log.e("StockSync", "Forbidden for stock $stockId during $operation - insufficient permissions")
    }

    private fun handleBadGateway(stockId: String, operation: String) {
        Log.w("StockSync", "Bad gateway for stock $stockId during $operation - retry later")
    }

    private fun handleGatewayTimeout(stockId: String, operation: String) {
        Log.w("StockSync", "Gateway timeout for stock $stockId during $operation - retry later")
    }

    private fun handleClientError(stockId: String, operation: String, statusCode: Int) {
        Log.e("StockSync", "Client error $statusCode for stock $stockId during $operation")
    }

    private fun handleGenericHttpError(stockId: String, operation: String, statusCode: Int) {
        Log.e("StockSync", "HTTP error $statusCode for stock $stockId during $operation")
    }

    // --- Network and Generic Error Handlers ---
    private fun handleNetworkError(stockId: String, operation: String, e: IOException) {
        Log.w("StockSync", "Network error for stock $stockId during $operation: ${e.message}")
    }

    private fun handleUnknownError(stockId: String, operation: String, e: Exception) {
        Log.e("StockSync", "Unknown error for stock $stockId during $operation: ${e.message}", e)
    }

    // --- Handle deleted stock (404 scenario) ---
    private suspend fun handleDeletedStock(stockId: String, operationType: String) {
        try {
            localDataSource.deleteLocalStockById(stockId)
            Log.i("StockSync", "Stock $stockId was deleted on server during $operationType, removed locally")
        } catch (e: Exception) {
            Log.e("StockSync", "Failed to clean up locally deleted stock $stockId", e)
        }
    }

    // --- Push pending operations ---
    override suspend fun push(operations: List<PendingOperation>) {
        for (operation in operations) {
            syncOperation(operation, operations)
        }
    }

    override suspend fun hasCachedData(): Boolean = localDataSource.getLocalStockCount() != 0

    private suspend fun syncOperation(currentOperation: PendingOperation, allOperations: List<PendingOperation>) {
        val stock = currentOperation.parsePayload<RemoteStock>()
        val totalPending = allOperations.count { it.entityId == currentOperation.entityId }

        localDataSource.updateSyncStatus(currentOperation.entityId, SyncStatus.SYNCING)

        Log.i("StockSyncOperation", "Start operation ${currentOperation.operationType} ${currentOperation.entityType} ${currentOperation.entityId}")

        try {
            when (currentOperation.operationType) {
                OperationType.CREATE -> pushCreatedStock(stock)
                OperationType.UPDATE -> pushUpdatedStock(stock)
                OperationType.UPDATE_STOCK_QUANTITY -> pushUpdatedStockQuantity(stock)
                OperationType.START_SESSION -> {}  // session-specific operations
                OperationType.END_SESSION -> {}    // session-specific operations
                OperationType.DELETE -> pushDeletedStock(stock)
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
                Log.e("StockSyncOperation", "Failed operation ${currentOperation.operationType} ${currentOperation.entityType} ${currentOperation.entityId}", e)
            }
        }
    }

    override fun getEntity(): EntityType = EntityType.Stock

    // --- Sync Logic ---
    private fun shouldPerformFullSync(lastSync: Long?): Boolean {
        return when {
            lastSync == null -> true
            System.currentTimeMillis() - lastSync > SyncConfig.FULL_SYNC_THRESHOLD_MS -> true
            else -> false
        }
    }

    private suspend fun getLastSyncTimestamp(): Long? {
        return syncMetadataDao.getLastSyncTimestamp(EntityType.Stock)
    }

    private suspend fun updateLastSyncTimestamp(timestamp: Long, success: Boolean = true, error: String? = null) {
        syncMetadataDao.updateLastSync(EntityType.Stock, timestamp, success)
        if (error != null) {
            syncMetadataDao.updateSyncStatus(EntityType.Stock, false, error)
        }
    }

    private suspend fun processIncrementalChanges(organization: String, since: Long) {
        try {
            Log.i("StockSync", "Performing incremental sync since ${java.util.Date(since)}")

            val adjustedSince = since - (45 * 60 * 1000) // 45 minutes buffer

            val changes = remoteDataSource.getRemoteStocksChangesSince(organization, adjustedSince)
            val remoteStockIds = remoteDataSource.getRemoteStockIds(organization)

            val domainStocks = changes.map { stockMapper.mapRemoteToDomain(it) }
            val localEntities = domainStocks.map { stockMapper.mapDomainToLocal(it) }

            localEntities.forEach { entity ->
                localDataSource.insertLocalStock(entity)
            }

            cleanupDeletedStocks(organization, remoteStockIds)

            Log.i("StockSync", "Incremental sync completed: ${changes.size} updates")

        } catch (e: Exception) {
            Log.w("StockSync", "Incremental sync failed, will fall back to full sync", e)
            throw e
        }
    }

    private suspend fun processFullSync(organization: String) {
        Log.i("StockSync", "Performing full sync")
        val remoteStocks = remoteDataSource.getRemoteStocks(organization)
        val remoteStockIds = remoteDataSource.getRemoteStockIds(organization)

        val domainStocks = remoteStocks.map { stockMapper.mapRemoteToDomain(it) }
        val localEntities = domainStocks.map { stockMapper.mapDomainToLocal(it) }

        localEntities.forEach {
            localDataSource.insertLocalStock(it)
        }

        cleanupDeletedStocks(organization, remoteStockIds)

        Log.i("StockSync", "Full sync completed: ${localEntities.size} stocks")
    }

    // --- Full pull from remote ---
    override suspend fun pullAll(organization: String) {
        Log.i("StockSync", "Sync started for organization: $organization")

        val lastSync = getLastSyncTimestamp()
        val shouldFullSync = shouldPerformFullSync(lastSync)

        Log.w("StockSync", "LastSync ${lastSync.toString()} should sync: $shouldFullSync")

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
                    Log.w("StockSync", "Incremental sync failed after $incrementalAttempts attempts, forcing full sync")
                    try {
                        processFullSync(organization)
                        success = true
                    } catch (fullSyncError: Exception) {
                        syncError = fullSyncError.message
                        Log.e("StockSync", "Full sync also failed", fullSyncError)
                    }
                } else {
                    Log.w("StockSync", "Incremental sync attempt $incrementalAttempts failed, retrying...", e)
                    delay(1000L * incrementalAttempts)
                }
            }
        }

        if (success) {
            updateLastSyncTimestamp(System.currentTimeMillis(), true)
            Log.i("StockSync", "Sync completed successfully")
        } else {
            updateLastSyncTimestamp(lastSync ?: 0L, false, syncError)
            Log.e("StockSync", "Sync failed after all attempts")
            throw RuntimeException("Sync failed: $syncError")
        }
    }

    private suspend fun cleanupDeletedStocks(
        organization: String,
        allRemoteIds: List<String>,
        maxDeletions: Int = 500
    ): CleanupResult {
        try {
            Log.d("StockSync", "Starting deletion cleanup for $organization...")

            val localIds = localDataSource.getLocalStockIds().toSet()
            val remoteIdsSet = allRemoteIds.toSet()
            val mustBeDeletedLocally = localIds.subtract(remoteIdsSet)

            return if (mustBeDeletedLocally.isNotEmpty()) {
                val idsToDelete = if (mustBeDeletedLocally.size > maxDeletions) {
                    Log.w("StockSync", "Too many deletions (${mustBeDeletedLocally.size}), limiting to $maxDeletions")
                    mustBeDeletedLocally.take(maxDeletions)
                } else {
                    mustBeDeletedLocally.toList()
                }

                Log.i("StockSync", "Found ${mustBeDeletedLocally.size} stocks to delete (processing ${idsToDelete.size})")

                idsToDelete.forEach { deletedId ->
                    localDataSource.deleteLocalStockById(deletedId)
                }

                CleanupResult(
                    success = true,
                    deletedCount = idsToDelete.size,
                    totalFound = mustBeDeletedLocally.size,
                    limited = mustBeDeletedLocally.size > maxDeletions
                ).also {
                    Log.i("StockSync", "Deletion cleanup completed: $it")
                }

            } else {
                CleanupResult(success = true, deletedCount = 0).also {
                    Log.d("StockSync", "No deleted stocks found")
                }
            }

        } catch (e: Exception) {
            Log.e("StockSync", "Failed during deletion cleanup", e)
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