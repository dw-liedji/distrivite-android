package com.datavite.distrivite.data.repository

import com.datavite.distrivite.data.mapper.LeaveMapper
import com.datavite.distrivite.data.local.SyncType
import com.datavite.distrivite.data.local.datasource.LeaveLocalDataSource
import com.datavite.distrivite.data.remote.datasource.LeaveRemoteDataSource
import com.datavite.distrivite.domain.model.DomainLeave
import com.datavite.distrivite.domain.repository.LeaveRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class LeaveRepositoryImpl @Inject constructor (
    private val localDataSource: LeaveLocalDataSource,
    private val remoteDataSource: LeaveRemoteDataSource,
    private val leaveMapper: LeaveMapper
) : LeaveRepository {
    override suspend fun getLeaveById(id: String): DomainLeave? {
        localDataSource.getLeaveById(id)?.let {
            return leaveMapper.mapLocalToDomain(it)
        }
        return null
    }

    override suspend fun getLeavesFlow(): Flow<List<DomainLeave>> {
        return localDataSource.getLeavesFlow().map { leaves ->
            leaves.map { leaveMapper.mapLocalToDomain(it) }
        }
    }

    override suspend fun getLeaveForEmployeeOnDate(employeeId: String, date: String): DomainLeave? {
        localDataSource.getLeaveForEmployeeOnDate(employeeId, date)?.let {
            return leaveMapper.mapLocalToDomain(it)
        }
        return null
    }

    override suspend fun createLeave(organization: String, leave: DomainLeave) {
        try {
            val remoteLeave = leaveMapper.mapDomainToRemote(leave)
            val createdRemoteLeave = remoteDataSource.createLeave(organization, remoteLeave)
            val createdDomainLeave = leaveMapper.mapRemoteToDomain(createdRemoteLeave)
            val createdLocalLeave = leaveMapper.mapDomainToLocal(createdDomainLeave, SyncType.SYNCED)
            localDataSource.saveLeave(createdLocalLeave)
        } catch (e: Exception) {
            val localLeave = leaveMapper.mapDomainToLocal(leave, SyncType.PENDING_CREATION)
            localDataSource.saveLeave(localLeave)
        }
    }

    override suspend fun updateLeave(organization: String, leave: DomainLeave) {
        try {
            val remoteLeave = leaveMapper.mapDomainToRemote(leave)
            val updatedRemoteLeave = remoteDataSource.updateLeave(organization, remoteLeave)
            val updatedDomainLeave = leaveMapper.mapRemoteToDomain(updatedRemoteLeave)
            val updatedLocalLeave = leaveMapper.mapDomainToLocal(updatedDomainLeave, SyncType.SYNCED)
            localDataSource.saveLeave(updatedLocalLeave)
        } catch (e: Exception) {
            val localLeave = leaveMapper.mapDomainToLocal(leave, SyncType.PENDING_MODIFICATION)
            localDataSource.saveLeave(localLeave)
        }
    }

    override suspend fun deleteLeave(organization: String, leave: DomainLeave) {
        try {
            val remoteLeave = leaveMapper.mapDomainToRemote(leave)
            val deletedLeave = remoteDataSource.deleteLeave(organization, remoteLeave)
            localDataSource.deleteLeave(deletedLeave.id)
        } catch (e: Exception) {
            localDataSource.markAsPendingDeletion(leave.id)
        }
    }

    override suspend fun syncLeaves(organization: String) {
        // Fetch unSynced leaves from the local database
        val unSyncedLeaves = localDataSource.getUnSyncedLeaves()

        // Try to sync them with the server
        for (leave in unSyncedLeaves) {
            try {
                when (leave.syncType) {
                    SyncType.PENDING_CREATION -> createLeave(organization, leaveMapper.mapLocalToDomain(leave))
                    SyncType.PENDING_MODIFICATION -> {
                        // Should implement conflict resolution between remote and local changes
                        try {
                            val domainLeave =  leaveMapper.mapLocalToDomain(leave)
                            val remoteLeave = leaveMapper.mapDomainToRemote(domainLeave)
                            remoteDataSource.updateLeave(organization, remoteLeave)
                            val localLeave = leaveMapper.mapDomainToLocal(domainLeave, SyncType.SYNCED)
                            localDataSource.saveLeave(localLeave)
                        } catch (e: Exception) {
                            val domainLeave =  leaveMapper.mapLocalToDomain(leave)
                            val localLeave = leaveMapper.mapDomainToLocal(domainLeave, SyncType.PENDING_MODIFICATION)
                            localDataSource.saveLeave(localLeave)
                        }
                    }
                    SyncType.PENDING_DELETION -> deleteLeave(
                        organization, leaveMapper.mapLocalToDomain(leave)
                    )
                    else -> {}
                }

            } catch (e: Exception) {
                // Handle the sync failure (e.g., log error, retry later)
            }
        }

        fetchLatestRemoteLeavesAndUpdateLocalLeaves(organization)
    }

    private suspend fun fetchLatestRemoteLeavesAndUpdateLocalLeaves(organization: String){
        try {
            val remoteLeaves = remoteDataSource.getLeaves(organization)
            val domainLeaves = remoteLeaves.map { leaveMapper.mapRemoteToDomain(it) }
            val localLeaves = domainLeaves.map { leaveMapper.mapDomainToLocal(it, SyncType.SYNCED) }
            localDataSource.saveLeaves(localLeaves)
        } catch (e: Exception) {
            // If fetching from remote fails, fallback to local
        }
    }

}