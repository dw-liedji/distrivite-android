package com.datavite.distrivite.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.datavite.distrivite.data.local.SyncType
import com.datavite.distrivite.data.local.model.LocalClaim
import kotlinx.coroutines.flow.Flow

@Dao
interface ClaimDao {

    @Query("SELECT * FROM claims ORDER BY created DESC")
    fun getClaimsFlow(): Flow<List<LocalClaim>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveClaim(claim: LocalClaim)

    @Query("DELETE FROM claims WHERE id = :id")
    suspend fun deleteClaim(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateClaims(claims: List<LocalClaim>)

    @Update
    suspend fun updateClaim(claim: LocalClaim)

    @Query("SELECT * FROM claims WHERE id = :id LIMIT 1")
    suspend fun getClaimById(id: String): LocalClaim?

    @Query("SELECT * FROM claims WHERE employeeId = :employeeId AND date = :date LIMIT 1")
    suspend fun getClaimForEmployeeOnDate(employeeId: String, date: String): LocalClaim?

    @Query("SELECT * FROM claims WHERE syncType != :syncType ORDER BY created DESC")
    suspend fun getUnSyncedClaims(syncType: SyncType = SyncType.SYNCED): List<LocalClaim>

    @Query("SELECT * FROM claims WHERE syncType = :syncType ORDER BY created DESC")
    suspend fun getClaimsBySyncType(syncType: SyncType): List<LocalClaim>

    @Update
    suspend fun markClaimAsSynced(claim: LocalClaim)

    @Query("UPDATE claims SET syncType = :syncType WHERE id = :id")
    suspend fun markAsPendingDeletion(id: String, syncType: SyncType = SyncType.PENDING_DELETION)

    @Query("UPDATE claims SET syncType = :syncType WHERE id = :id")
    suspend fun updateClaimSyncType(id: String, syncType: SyncType)
}
