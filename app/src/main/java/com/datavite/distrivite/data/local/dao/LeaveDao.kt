package com.datavite.distrivite.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.datavite.distrivite.data.local.SyncType
import com.datavite.distrivite.data.local.model.LocalLeave
import kotlinx.coroutines.flow.Flow

@Dao
interface LeaveDao {

    @Query("SELECT * FROM leaves ORDER BY created DESC")
    fun getLeavesFlow(): Flow<List<LocalLeave>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveLeave(leave: LocalLeave)

    @Query("DELETE FROM leaves WHERE id = :id")
    suspend fun deleteLeave(id: String)


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateLeaves(leaves: List<LocalLeave>)

    @Update
    suspend fun updateLeave(leave: LocalLeave)

    @Query("SELECT * FROM leaves WHERE id = :id LIMIT 1")
    suspend fun getLeaveById(id: String): LocalLeave?
    @Query("SELECT * FROM leaves WHERE employeeId = :employeeId AND :date BETWEEN startDate AND endDate LIMIT 1")
    suspend fun getLeaveForEmployeeOnDate(employeeId: String, date: String): LocalLeave?

    @Query("SELECT * FROM leaves WHERE syncType != :syncType ORDER BY created DESC")
    suspend fun getUnSyncedLeaves(syncType: SyncType=SyncType.SYNCED): List<LocalLeave>

    @Query("SELECT * FROM leaves WHERE syncType = :syncType")
    suspend fun getLeavesBySyncType(syncType: SyncType): List<LocalLeave>

    @Update
    suspend fun markLeaveAsSynced(leave: LocalLeave)

    @Query("UPDATE leaves SET syncType = :syncType WHERE id = :id")
    suspend fun markAsPendingDeletion(id: String, syncType: SyncType = SyncType.PENDING_DELETION)

    @Query("UPDATE leaves SET syncType = :syncType WHERE id = :id")
    suspend fun updateLeaveSyncType(id: String, syncType: SyncType)
}