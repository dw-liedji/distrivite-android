package com.datavite.distrivite.data.local.dao

import androidx.room.*
import com.datavite.distrivite.data.local.model.LocalBulkCreditPayment
import com.datavite.distrivite.data.local.model.SyncStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface LocalBulkCreditPaymentDao {

    // --- CRUD Operations ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bulkCreditPayment: LocalBulkCreditPayment)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bulkCreditPayments: List<LocalBulkCreditPayment>)

    @Update
    suspend fun update(bulkCreditPayment: LocalBulkCreditPayment)

    @Query("UPDATE localBulkCreditPayments SET syncStatus = :syncStatus WHERE id = :id")
    suspend fun updateSyncStatus(id: String, syncStatus: SyncStatus)

    @Delete
    suspend fun delete(bulkCreditPayment: LocalBulkCreditPayment)

    @Query("DELETE FROM localBulkCreditPayments WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM localBulkCreditPayments")
    suspend fun deleteAll()

    // --- Query Operations ---
    @Query("SELECT * FROM localBulkCreditPayments WHERE id = :id")
    suspend fun getById(id: String): LocalBulkCreditPayment?

    @Query("SELECT * FROM localBulkCreditPayments WHERE customerId = :customerId ORDER BY created DESC")
    fun getByCustomerId(customerId: String): Flow<List<LocalBulkCreditPayment>>

    @Query("SELECT * FROM localBulkCreditPayments WHERE orgId = :orgId ORDER BY created DESC")
    suspend fun getByOrgId(orgId: String): List<LocalBulkCreditPayment>

    @Query("SELECT * FROM localBulkCreditPayments WHERE orgSlug = :orgSlug ORDER BY created DESC")
    suspend fun getByOrgSlug(orgSlug: String): List<LocalBulkCreditPayment>

    @Query("SELECT * FROM localBulkCreditPayments WHERE syncStatus = :syncStatus ORDER BY created DESC")
    suspend fun getBySyncStatus(syncStatus: SyncStatus): List<LocalBulkCreditPayment>

    @Query("SELECT * FROM localBulkCreditPayments WHERE syncStatus IN (:statuses) ORDER BY created DESC")
    suspend fun getBySyncStatuses(statuses: List<SyncStatus>): List<LocalBulkCreditPayment>

    @Query("SELECT * FROM localBulkCreditPayments ORDER BY created DESC")
    fun getAll(): Flow<List<LocalBulkCreditPayment>>

    @Query("SELECT * FROM localBulkCreditPayments ORDER BY created DESC")
    suspend fun getAllSync(): List<LocalBulkCreditPayment>

    // --- Count Operations ---
    @Query("SELECT COUNT(*) FROM localBulkCreditPayments")
    suspend fun count(): Int

    @Query("SELECT COUNT(*) FROM localBulkCreditPayments WHERE syncStatus = :syncStatus")
    suspend fun countBySyncStatus(syncStatus: SyncStatus): Int

    @Query("SELECT COUNT(*) FROM localBulkCreditPayments WHERE orgId = :orgId")
    suspend fun countByOrgId(orgId: String): Int

    @Query("SELECT COUNT(*) FROM localBulkCreditPayments WHERE customerId = :customerId")
    suspend fun countByCustomerId(customerId: String): Int

    // --- ID Operations ---
    @Query("SELECT id FROM localBulkCreditPayments")
    suspend fun getAllIds(): List<String>

    @Query("SELECT id FROM localBulkCreditPayments WHERE orgId = :orgId")
    suspend fun getIdsByOrgId(orgId: String): List<String>

    @Query("SELECT id FROM localBulkCreditPayments WHERE syncStatus = :syncStatus")
    suspend fun getIdsBySyncStatus(syncStatus: SyncStatus): List<String>

    // --- Batch Operations ---
    @Transaction
    suspend fun upsertAll(bulkCreditPayments: List<LocalBulkCreditPayment>) {
        bulkCreditPayments.forEach { insert(it) }
    }

    @Query("SELECT * FROM localBulkCreditPayments WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<String>): List<LocalBulkCreditPayment>

    // --- Sync Specific Queries ---
    @Query("SELECT * FROM localBulkCreditPayments WHERE syncStatus = :syncStatus AND orgSlug = :orgSlug")
    suspend fun getBySyncStatusAndOrgSlug(syncStatus: SyncStatus, orgSlug: String): List<LocalBulkCreditPayment>

    @Query("SELECT * FROM localBulkCreditPayments WHERE syncStatus != :excludedStatus ORDER BY created DESC")
    suspend fun getAllExcludingStatus(excludedStatus: SyncStatus): List<LocalBulkCreditPayment>

    // --- Date Range Query (if needed) ---
    @Query("SELECT * FROM localBulkCreditPayments WHERE created BETWEEN :startDate AND :endDate ORDER BY created DESC")
    fun getByDateRange(startDate: String, endDate: String): Flow<List<LocalBulkCreditPayment>>
}