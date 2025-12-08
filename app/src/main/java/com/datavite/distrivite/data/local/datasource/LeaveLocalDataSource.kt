package com.datavite.distrivite.data.local.datasource

import com.datavite.distrivite.data.local.SyncType
import com.datavite.distrivite.data.local.model.LocalLeave
import kotlinx.coroutines.flow.Flow

interface LeaveLocalDataSource {

    fun getLeavesFlow(): Flow<List<LocalLeave>>

    suspend fun saveLeave(leave: LocalLeave)

    suspend fun deleteLeave(id: String)

    suspend fun saveLeaves(leaves: List<LocalLeave>)

    suspend fun updateLeave(leave: LocalLeave)

    suspend fun getLeaveById(id: String): LocalLeave?

    suspend fun getLeaveForEmployeeOnDate(employeeId: String, date: String): LocalLeave?

    suspend fun getUnSyncedLeaves(syncType: SyncType = SyncType.SYNCED): List<LocalLeave>

    suspend fun getLeavesBySyncType(syncType: SyncType): List<LocalLeave>

    suspend fun markLeaveAsSynced(leave: LocalLeave)

    suspend fun markAsPendingDeletion(id: String, syncType: SyncType = SyncType.PENDING_DELETION)

    suspend fun updateLeaveSyncType(id: String, syncType: SyncType)


}
