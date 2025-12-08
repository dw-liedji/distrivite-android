package com.datavite.distrivite.data.sync

import android.util.Log
import com.datavite.distrivite.data.local.dao.PendingOperationDao
import com.datavite.distrivite.data.local.model.PendingOperation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import javax.inject.Inject
class SyncOrchestrator @Inject constructor(
    private val pendingOperationDao: PendingOperationDao,
    // <-- Array with "out" (instead of list) for polymorphism and collection injection support
    private val syncServices: Array<out SyncService>,
) {
    private val maxRetries = 10000

    private val lock = Mutex()

    suspend fun push(organization: String) = lock.withLock {

        val operations = pendingOperationDao
            .getNextOperationsEligibleForRetry(maxRetries = maxRetries, limit = 20)
            .sortedBy { it.createdAt }

        if (operations.isEmpty()) return

        val operationsByEntityType: Map<EntityType, List<PendingOperation>> =
            operations.groupBy { it.entityType }

        for (service in syncServices) {
            val entityType = service.getEntity()
            val entityOperations = operationsByEntityType[entityType] ?: continue

            try {
                service.push(entityOperations)
            } catch (e: Exception) {
                Log.e("SyncOrchestrator", "Sync failed for $entityType", e)
            }
        }

        val knownTypes = syncServices.map { it.getEntity() }.toSet()
        val unknownTypes = operationsByEntityType.keys - knownTypes
        if (unknownTypes.isNotEmpty()) {
            Log.w("SyncOrchestrator", "Operations found with no matching SyncService: $unknownTypes")
        }
    }


    suspend fun pushAndPullAll(organization: String, isSerial: Boolean = false) {
        Log.i("syncLocalDataWithServer", "syncLocalDataWithServer stared")
        push(organization)
        if (isSerial) pullAllInSerial(organization) else pullAllInParallel(organization)
    }

    suspend fun pullAllInSerial(organization: String) {

        for (service in syncServices) {
            try {
                service.pullAll(organization)
            } catch (e: Exception) {
                Log.e("SyncOrchestrator", "FetchAll failed for ${service.getEntity()}", e)
            }
        }
    }

    suspend fun pullAllInParallel(organization: String) = withContext(Dispatchers.IO) {
        val deferredFetchAll = syncServices.map { service ->
            async {
                try {
                    service.pullAll(organization)
                } catch (e: Exception) {
                    Log.e("SyncOrchestrator", "FetchAll failed for ${service.getEntity()}", e)
                }
            }
        }
        deferredFetchAll.awaitAll()
    }

    suspend fun pullAllInParallelIfNoData(organization: String) {
        if (!hasCachedData()) {
            Log.i("SyncOrchestrator", "No cached data. Starting sync for $organization")
            pullAllInParallel(organization)
        } else {
            Log.i("SyncOrchestrator", "Data already cached. Skipping initial sync for $organization")
        }
    }


    suspend fun hasCachedData() : Boolean = withContext(Dispatchers.IO) {
        if (syncServices.isEmpty()) return@withContext false
        val results = syncServices.map { async { it.hasCachedData() } }
        return@withContext results.awaitAll().all { it }
    }
}
