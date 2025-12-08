package com.datavite.distrivite.data.repository

import com.datavite.distrivite.data.mapper.WorkingPeriodMapper
import com.datavite.distrivite.data.local.SyncType
import com.datavite.distrivite.data.local.datasource.WorkingPeriodLocalDataSource
import com.datavite.distrivite.data.remote.datasource.WorkingPeriodRemoteDataSource
import com.datavite.distrivite.domain.model.DomainWorkingPeriod
import com.datavite.distrivite.domain.repository.WorkingPeriodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class WorkingPeriodRepositoryImpl @Inject constructor (
    private val localDataSource: WorkingPeriodLocalDataSource,
    private val remoteDataSource: WorkingPeriodRemoteDataSource,
    private val workingPeriodMapper: WorkingPeriodMapper
) : WorkingPeriodRepository {

    override suspend fun getWorkingPeriodsFlow(): Flow<List<DomainWorkingPeriod>> {
        return localDataSource.getWorkingPeriodsFlow().map { sessions ->
            sessions.map { workingPeriodMapper.mapLocalToDomain(it) }
        }
    }

    override suspend fun getWorkingPeriodsFor(searchQuery: String): List<DomainWorkingPeriod> {
        return localDataSource.getSearchWorkingPeriodsFor(searchQuery).map { workingPeriodMapper.mapLocalToDomain(it) }
    }

    override suspend fun createWorkingPeriod(organization: String, session: DomainWorkingPeriod) {
        try {
            val remoteWorkingPeriod = workingPeriodMapper.mapDomainToRemote(session)
            remoteDataSource.createWorkingPeriod(organization, remoteWorkingPeriod)
            val localWorkingPeriod = workingPeriodMapper.mapDomainToLocal(session, SyncType.SYNCED)
            localDataSource.saveWorkingPeriod(localWorkingPeriod)
        } catch (e: Exception) {
            val localWorkingPeriod = workingPeriodMapper.mapDomainToLocal(session, SyncType.PENDING_CREATION)
            localDataSource.saveWorkingPeriod(localWorkingPeriod)
        }
    }

    override suspend fun updateWorkingPeriod(organization: String, session: DomainWorkingPeriod) {
        try {
            val remoteWorkingPeriod = workingPeriodMapper.mapDomainToRemote(session)
            remoteDataSource.updateWorkingPeriod(organization, remoteWorkingPeriod)
            val localWorkingPeriod = workingPeriodMapper.mapDomainToLocal(session, SyncType.SYNCED)
            localDataSource.saveWorkingPeriod(localWorkingPeriod)
        } catch (e: Exception) {
            val localWorkingPeriod = workingPeriodMapper.mapDomainToLocal(session, SyncType.PENDING_MODIFICATION)
            localDataSource.saveWorkingPeriod(localWorkingPeriod)
        }
    }

    override suspend fun deleteWorkingPeriod(organization: String, sessionId: String) {
        try {
            remoteDataSource.deleteWorkingPeriod(organization, sessionId)
            localDataSource.deleteWorkingPeriod(sessionId)
        } catch (e: Exception) {
            localDataSource.markAsPendingDeletion(sessionId)
        }
    }

    override suspend fun getWorkingPeriodsByIdsForDay(ids: List<String>, dayId:Int): List<DomainWorkingPeriod> {
        val localWorkingPeriods = localDataSource.getWorkingPeriodsByIdsForDay(ids, dayId)
        return localWorkingPeriods.map { workingPeriodMapper.mapLocalToDomain(it) }
    }

    override suspend fun getWorkingPeriodsByIds(ids: List<String>): List<DomainWorkingPeriod> {
        val localWorkingPeriods = localDataSource.getWorkingPeriodsByIds(ids)
        return localWorkingPeriods.map { workingPeriodMapper.mapLocalToDomain(it) }
    }

    override suspend fun syncWorkingPeriods(organization: String) {
        // Fetch unSynced sessions from the local database
        val unSyncedWorkingPeriods = localDataSource.getUnSyncedWorkingPeriods()

        // Try to sync them with the server
        for (session in unSyncedWorkingPeriods) {
            try {
                when (session.syncType) {
                    SyncType.PENDING_CREATION -> createWorkingPeriod(organization, workingPeriodMapper.mapLocalToDomain(session))
                    SyncType.PENDING_MODIFICATION -> {
                        // Should implement conflict resolution between remote and local changes
                        try {
                            val domainWorkingPeriod =  workingPeriodMapper.mapLocalToDomain(session)
                            val remoteWorkingPeriod = workingPeriodMapper.mapDomainToRemote(domainWorkingPeriod)
                            remoteDataSource.updateWorkingPeriod(organization, remoteWorkingPeriod)
                            val localWorkingPeriod = workingPeriodMapper.mapDomainToLocal(domainWorkingPeriod, SyncType.SYNCED)
                            localDataSource.saveWorkingPeriod(localWorkingPeriod)
                        } catch (e: Exception) {
                            val domainWorkingPeriod =  workingPeriodMapper.mapLocalToDomain(session)
                            val localWorkingPeriod = workingPeriodMapper.mapDomainToLocal(domainWorkingPeriod, SyncType.PENDING_MODIFICATION)
                            localDataSource.saveWorkingPeriod(localWorkingPeriod)
                        }
                    }
                    SyncType.PENDING_DELETION -> deleteWorkingPeriod(organization, session.id)
                    else -> {}
                }

            } catch (e: Exception) {
                // Handle the sync failure (e.g., log error, retry later)
            }
        }

        fetchLatestRemoteWorkingPeriodsAndUpdateLocalWorkingPeriods(organization)
    }

    private suspend fun fetchLatestRemoteWorkingPeriodsAndUpdateLocalWorkingPeriods(organization: String){
        try {
            val remoteWorkingPeriods = remoteDataSource.getWorkingPeriods(organization)
            val domainWorkingPeriods = remoteWorkingPeriods.map { workingPeriodMapper.mapRemoteToDomain(it) }
            val localWorkingPeriods = domainWorkingPeriods.map { workingPeriodMapper.mapDomainToLocal(it, SyncType.SYNCED) }
            localDataSource.saveWorkingPeriods(localWorkingPeriods)
        } catch (e: Exception) {
            // If fetching from remote fails, fallback to local
        }
    }
}