package com.datavite.distrivite.data.sync

import android.util.Log
import com.datavite.distrivite.data.local.dao.SyncMetadataDao
import com.datavite.distrivite.data.local.model.SyncMetadata
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class SyncMetadataManager @Inject constructor(
    private val syncMetadataDao: SyncMetadataDao
) {
    suspend fun ensureInitialized() {
        try {
            val entities = EntityType.entries.toTypedArray()
            var initializedCount = 0

            entities.forEach { entityType ->
                val existing = syncMetadataDao.getSyncMetadata(entityType)
                if (existing == null) {
                    Log.w("SyncMetadata", "⚠️ Missing sync metadata for $entityType, creating...")
                    syncMetadataDao.insertOrUpdate(
                        SyncMetadata(
                            entityType = entityType,
                            lastSyncTimestamp = 0L,
                            lastSyncSuccess = false,
                            syncVersion = 1,
                            retryCount = 0
                        )
                    )
                    initializedCount++
                }
            }

            if (initializedCount > 0) {
                Log.i("SyncMetadata", "✅ Initialized $initializedCount missing sync metadata entries")
            }

        } catch (e: Exception) {
            Log.e("SyncMetadata", "❌ Failed to initialize sync metadata", e)
        }
    }
}