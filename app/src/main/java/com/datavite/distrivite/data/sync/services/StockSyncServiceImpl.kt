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
import com.datavite.distrivite.data.sync.OperationScope
import com.datavite.distrivite.data.sync.OperationType
import com.datavite.distrivite.data.sync.SyncConfig
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
    private suspend fun pushCreatedStock(remoteStock: RemoteStock, currentOperation: PendingOperation) {
        try {
            remoteDataSource.createRemoteStock(remoteStock.orgSlug, remoteStock)
            val updatedDomain = stockMapper.mapRemoteToDomain(remoteStock)
            val localEntity = stockMapper.mapDomainToLocal(updatedDomain)

            localDataSource.insertLocalStock(localEntity)
            Log.i("StockSync", "Successfully synced created stock ${remoteStock.id}")
        } catch (e: Exception) {
            val handled = handleStockSyncException(e, remoteStock.id, currentOperation)
            if (!handled) throw e
        }
    }

    // --- Push UPDATE ---
    private suspend fun pushUpdatedStock(remoteStock: RemoteStock, currentOperation: PendingOperation) {
        try {
            remoteDataSource.updateRemoteStock(remoteStock.orgSlug, remoteStock)
            val updatedDomain = stockMapper.mapRemoteToDomain(remoteStock)
            val localEntity = stockMapper.mapDomainToLocal(updatedDomain)

            localDataSource.insertLocalStock(localEntity)
            Log.i("StockSync", "Successfully synced updated stock ${remoteStock.id}")
        } catch (e: Exception) {
            val handled = handleStockSyncException(e, remoteStock.id, currentOperation)
            if (!handled) throw e
        }
    }

    // --- Push UPDATE QUANTITY ---
    private suspend fun pushUpdatedStockQuantity(remoteStock: RemoteStock, currentOperation: PendingOperation) {
        try {
            Log.i("StockSync", "Syncing updated stock quantity ${remoteStock.id} started")

            val remote = remoteDataSource.updateRemoteStockQuantity(remoteStock.orgSlug, remoteStock)
            val updatedDomain = stockMapper.mapRemoteToDomain(remote)
            val localEntity = stockMapper.mapDomainToLocal(updatedDomain)

            localDataSource.insertLocalStock(localEntity)
            Log.i("StockSync", "Successfully synced updated stock quantity ${remoteStock.id}")
        } catch (e: Exception) {
            val handled = handleStockSyncException(e, remoteStock.id, currentOperation)
            if (!handled) throw e
        }
    }

    // --- Push DELETE ---
    private suspend fun pushDeletedStock(remoteStock: RemoteStock, currentOperation: PendingOperation) {
        try {
            remoteDataSource.deleteRemoteStock(remoteStock.orgSlug, remoteStock.id)
            Log.i("StockSync", "Successfully synced deleted stock ${remoteStock.id}")
        } catch (e: Exception) {
            val handled = handleStockSyncException(e, remoteStock.id, currentOperation)
            if (!handled) throw e
        }
    }

    // --- Comprehensive Exception Handling ---
    private suspend fun handleStockSyncException(e: Exception, stockId: String, currentOperation: PendingOperation): Boolean {
        return when (e) {
            is HttpException -> {
                when (e.code()) {
                    HTTP_NOT_FOUND -> handleNotFound(stockId, currentOperation)
                    HTTP_CONFLICT -> handleConflict(stockId, currentOperation)
                    HTTP_UNAVAILABLE -> handleServiceUnavailable(stockId, currentOperation)
                    HTTP_INTERNAL_ERROR -> handleServerError(stockId, currentOperation)
                    HTTP_BAD_REQUEST -> handleBadRequest(stockId, currentOperation)
                    HTTP_UNAUTHORIZED -> handleUnauthorized(stockId, currentOperation)
                    HTTP_FORBIDDEN -> handleForbidden(stockId, currentOperation)
                    HTTP_BAD_GATEWAY -> handleBadGateway(stockId, currentOperation)
                    HTTP_GATEWAY_TIMEOUT -> handleGatewayTimeout(stockId, currentOperation)
                    in 400..499 -> handleClientError(stockId, currentOperation, e.code())
                    in 500..599 -> handleServerError(stockId, currentOperation, e.code())
                    else -> handleGenericHttpError(stockId, currentOperation, e.code())
                }
            }
            is IOException -> handleNetworkError(stockId, currentOperation, e)
            else -> handleUnknownError(stockId, currentOperation, e)
        }
    }

    // --- HTTP Status Code Handlers ---
    private suspend fun handleNotFound(stockId: String, operation: PendingOperation): Boolean {
        Log.i("StockSync", "Stock $stockId not found during ${operation.operationType} - removing locally")
        return try {
            localDataSource.deleteLocalStockById(stockId)
            Log.i("StockSync", "Stock $stockId was deleted on server, removed locally")
            true  // Exception handled successfully
        } catch (e: Exception) {
            Log.e("StockSync", "Failed to clean up locally deleted stock $stockId", e)
            false  // Exception not fully handled
        }
    }

    private suspend fun handleConflict(stockId: String, operation: PendingOperation): Boolean {
        Log.w("StockSync", "Conflict detected for stock $stockId during ${operation.operationType} - resolving...")

        return when (operation.operationType) {
            OperationType.CREATE -> resolveCreateConflict(stockId, operation)
            OperationType.UPDATE -> resolveUpdateConflict(stockId, operation)
            OperationType.UPDATE_STOCK_QUANTITY -> resolveUpdateQuantityConflict(stockId, operation)
            OperationType.DELETE -> resolveDeleteConflict(stockId, operation)
            else -> {
                Log.w("StockSync", "Unhandled conflict type for ${operation.operationType}")
                false
            }
        }
    }

    private suspend fun resolveCreateConflict(stockId: String, operation: PendingOperation): Boolean {
        try {
            // For CREATE conflict, stock already exists on server
            localDataSource.updateSyncStatus(operation.entityId, SyncStatus.SYNCED)
            Log.w("StockSync", "CREATE conflict resolved for $stockId - marked as synced")
            return true
        } catch (e: Exception) {
            Log.e("StockSync", "Failed to resolve CREATE conflict for $stockId", e)
            return false
        }
    }

    private suspend fun resolveUpdateConflict(stockId: String, operation: PendingOperation): Boolean {
        Log.w("StockSync", "UPDATE conflict for $stockId - keeping as pending for retry")
        return false  // Not handled, will retry
    }

    private suspend fun resolveUpdateQuantityConflict(stockId: String, operation: PendingOperation): Boolean {
        Log.w("StockSync", "UPDATE_STOCK_QUANTITY conflict for $stockId - keeping as pending for retry")
        return false  // Not handled, will retry
    }

    private suspend fun resolveDeleteConflict(stockId: String, operation: PendingOperation): Boolean {
        try {
            // For DELETE conflict, item might already be deleted
            localDataSource.deleteLocalStockById(stockId)
            Log.i("StockSync", "DELETE conflict resolved for $stockId - removed locally")
            return true
        } catch (e: Exception) {
            Log.e("StockSync", "Failed to resolve DELETE conflict for $stockId", e)
            return false
        }
    }

    private fun handleServiceUnavailable(stockId: String, operation: PendingOperation): Boolean {
        Log.w("StockSync", "Service unavailable for stock $stockId during ${operation.operationType} - retry later")
        return false  // Not handled, will retry
    }

    private fun handleServerError(stockId: String, operation: PendingOperation, statusCode: Int? = null): Boolean {
        val codeInfo = if (statusCode != null) " (code: $statusCode)" else ""
        Log.e("StockSync", "Server error for stock $stockId during ${operation.operationType}$codeInfo")
        return false  // Not handled, will retry
    }

    private fun handleBadRequest(stockId: String, operation: PendingOperation): Boolean {
        Log.e("StockSync", "Bad request for stock $stockId during ${operation.operationType} - check data format")
        return true  // Handled - bad request won't succeed on retry
    }

    private fun handleUnauthorized(stockId: String, operation: PendingOperation): Boolean {
        Log.e("StockSync", "Unauthorized for stock $stockId during ${operation.operationType} - authentication required")
        return true  // Handled - auth error needs user intervention
    }

    private fun handleForbidden(stockId: String, operation: PendingOperation): Boolean {
        Log.e("StockSync", "Forbidden for stock $stockId during ${operation.operationType} - insufficient permissions")
        return true  // Handled - permission error won't succeed on retry
    }

    private fun handleBadGateway(stockId: String, operation: PendingOperation): Boolean {
        Log.w("StockSync", "Bad gateway for stock $stockId during ${operation.operationType} - retry later")
        return false  // Not handled, will retry
    }

    private fun handleGatewayTimeout(stockId: String, operation: PendingOperation): Boolean {
        Log.w("StockSync", "Gateway timeout for stock $stockId during ${operation.operationType} - retry later")
        return false  // Not handled, will retry
    }

    private fun handleClientError(stockId: String, operation: PendingOperation, statusCode: Int): Boolean {
        Log.e("StockSync", "Client error $statusCode for stock $stockId during ${operation.operationType}")
        return true  // Handled - client errors won't succeed on retry without fixing
    }

    private fun handleGenericHttpError(stockId: String, operation: PendingOperation, statusCode: Int): Boolean {
        Log.e("StockSync", "HTTP error $statusCode for stock $stockId during ${operation.operationType}")
        return false  // Not handled, generic case
    }

    // --- Network and Generic Error Handlers ---
    private fun handleNetworkError(stockId: String, operation: PendingOperation, e: IOException): Boolean {
        Log.w("StockSync", "Network error for stock $stockId during ${operation.operationType}: ${e.message}")
        return false  // Not handled, will retry
    }

    private fun handleUnknownError(stockId: String, operation: PendingOperation, e: Exception): Boolean {
        Log.e("StockSync", "Unknown error for stock $stockId during ${operation.operationType}: ${e.message}", e)
        return false  // Not handled
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

        Log.i("StockSyncOperation", "Start operation ${currentOperation.operationType} ${currentOperation.entityType} ${currentOperation.entityId}")

        localDataSource.updateSyncStatus(currentOperation.entityId, SyncStatus.SYNCING)

        try {
            when (currentOperation.operationType) {
                OperationType.CREATE -> pushCreatedStock(stock, currentOperation)
                OperationType.UPDATE -> pushUpdatedStock(stock, currentOperation)
                OperationType.UPDATE_STOCK_QUANTITY -> pushUpdatedStockQuantity(stock, currentOperation)
                OperationType.START_SESSION, OperationType.END_SESSION -> {
                    // Session-specific operations - do nothing for now
                }
                OperationType.DELETE -> pushDeletedStock(stock, currentOperation)
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
                    else -> false  // UPDATE, UPDATE_STOCK_QUANTITY conflicts not handled
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

            Log.e("StockSyncOperation", "Failed operation ${currentOperation.operationType} ${currentOperation.entityType} ${currentOperation.entityId}", e)
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