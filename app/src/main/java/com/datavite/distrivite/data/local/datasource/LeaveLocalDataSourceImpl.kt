package com.datavite.distrivite.data.local.datasource

import com.datavite.distrivite.data.local.SyncType
import com.datavite.distrivite.data.local.dao.LeaveDao
import com.datavite.distrivite.data.local.model.LocalLeave
import kotlinx.coroutines.flow.Flow

class LeaveLocalDataSourceImpl(
    private val leaveDao: LeaveDao
) : LeaveLocalDataSource {

    override fun getLeavesFlow(): Flow<List<LocalLeave>> {
        return leaveDao.getLeavesFlow()
    }

    override suspend fun saveLeave(leave: LocalLeave) {
        leaveDao.saveLeave(leave)
    }

    override suspend fun deleteLeave(id: String) {
        leaveDao.deleteLeave(id)
    }

    override suspend fun saveLeaves(leaves: List<LocalLeave>) {
        leaveDao.insertOrUpdateLeaves(leaves)
    }

    override suspend fun updateLeave(leave: LocalLeave) {
        leaveDao.updateLeave(leave)
    }

    override suspend fun getLeaveById(id: String): LocalLeave? {
        return leaveDao.getLeaveById(id)
    }

    override suspend fun getLeaveForEmployeeOnDate(employeeId: String, date: String): LocalLeave? {
        return leaveDao.getLeaveForEmployeeOnDate(employeeId, date.toString())
    }

    override suspend fun getUnSyncedLeaves(syncType: SyncType): List<LocalLeave> {
        return leaveDao.getUnSyncedLeaves(syncType)
    }

    override suspend fun getLeavesBySyncType(syncType: SyncType): List<LocalLeave> {
        return leaveDao.getLeavesBySyncType(syncType)
    }

    override suspend fun markLeaveAsSynced(leave: LocalLeave) {
        leaveDao.markLeaveAsSynced(leave)
    }

    override suspend fun markAsPendingDeletion(id: String, syncType: SyncType) {
        leaveDao.markAsPendingDeletion(id, syncType)
    }

    override suspend fun updateLeaveSyncType(id: String, syncType: SyncType) {
        leaveDao.updateLeaveSyncType(id, syncType)
    }
}
