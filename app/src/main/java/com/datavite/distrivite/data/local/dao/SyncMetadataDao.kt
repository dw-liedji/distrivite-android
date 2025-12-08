package com.datavite.distrivite.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.datavite.distrivite.data.local.model.SyncMetadata
import com.datavite.distrivite.data.sync.EntityType

// SyncMetadataDao.kt
@Dao
interface SyncMetadataDao {

    @Query("SELECT * FROM sync_metadata WHERE entityType = :entityType")
    suspend fun getSyncMetadata(entityType: EntityType): SyncMetadata?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(syncMetadata: SyncMetadata)

    @Query("UPDATE sync_metadata SET lastSyncTimestamp = :timestamp, lastSyncSuccess = :success, lastError = NULL, retryCount = 0 WHERE entityType = :entityType")
    suspend fun updateLastSync(entityType: EntityType, timestamp: Long, success: Boolean = true)

    @Query("UPDATE sync_metadata SET lastSyncSuccess = :success, lastError = :error, retryCount = retryCount + 1 WHERE entityType = :entityType")
    suspend fun updateSyncStatus(entityType: EntityType, success: Boolean, error: String? = null)

    @Query("SELECT lastSyncTimestamp FROM sync_metadata WHERE entityType = :entityType")
    suspend fun getLastSyncTimestamp(entityType: EntityType): Long?

    @Query("DELETE FROM sync_metadata WHERE entityType = :entityType")
    suspend fun deleteByEntityType(entityType: EntityType)

    @Query("SELECT * FROM sync_metadata")
    suspend fun getAllSyncMetadata(): List<SyncMetadata>

    @Query("UPDATE sync_metadata SET retryCount = 0 WHERE entityType = :entityType")
    suspend fun resetRetryCount(entityType: EntityType)
}