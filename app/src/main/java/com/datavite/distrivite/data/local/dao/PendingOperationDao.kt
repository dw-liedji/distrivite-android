package com.datavite.distrivite.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.datavite.distrivite.data.local.model.PendingOperation
import com.datavite.distrivite.data.sync.EntityType
import com.datavite.distrivite.data.sync.OperationType
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingOperationDao {

    // 🔥 Insert or replace based on composite PK
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(operation: PendingOperation)

    // 🔥 Get next operations FIFO
    @Query("SELECT * FROM pending_operations ORDER BY createdAt ASC LIMIT :limit")
    suspend fun getNextOperations(limit: Int = 50): List<PendingOperation>

    // 🔥 Retry-eligible operations
    @Query("""
        SELECT * FROM pending_operations
        WHERE failedAttempts < :maxRetries
        ORDER BY createdAt ASC LIMIT :limit
    """)
    suspend fun getNextOperationsEligibleForRetry(
        limit: Int = 50,
        maxRetries: Int = 5
    ): List<PendingOperation>

    // 🔥 Delete using composite key (since id no longer exists)
    @Query("""
        DELETE FROM pending_operations 
        WHERE entityType = :entityType
        AND entityId = :entityId
        AND orgId = :orgId
        AND operationType = :operationType
    """)
    suspend fun deleteByKeys(
        entityType: EntityType,
        entityId: String,
        orgId: String,
        operationType: OperationType
    )

    // 🔥 Increment failure count
    @Query("""
        UPDATE pending_operations
        SET failedAttempts = failedAttempts + 1
        WHERE entityType = :entityType
        AND entityId = :entityId
        AND orgId = :orgId
        AND operationType = :operationType
    """)
    suspend fun incrementFailureCount(
        entityType: EntityType,
        entityId: String,
        orgId: String,
        operationType: OperationType
    )

    // 🔥 Required for some logic (get failure count)
    @Query("""
        SELECT failedAttempts FROM pending_operations
        WHERE entityType = :entityType
        AND entityId = :entityId
        AND orgId = :orgId
        AND operationType = :operationType
    """)
    suspend fun getFailureCount(
        entityType: EntityType,
        entityId: String,
        orgId: String,
        operationType: OperationType
    ): Int

    // 🔥 Monitoring flows
    @Query("SELECT COUNT(*) FROM pending_operations")
    fun getPendingCountFlow(): Flow<Int>

    @Query("SELECT * FROM pending_operations ORDER BY createdAt ASC")
    fun getAllPendingOperationsFlow(): Flow<List<PendingOperation>>
}
