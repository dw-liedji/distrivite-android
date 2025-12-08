package com.datavite.distrivite.data.repository

import com.datavite.distrivite.data.mapper.TeachingPeriodMapper
import com.datavite.distrivite.data.local.SyncType
import com.datavite.distrivite.data.local.datasource.TeachingPeriodLocalDataSource
import com.datavite.distrivite.data.remote.datasource.TeachingPeriodRemoteDataSource
import com.datavite.distrivite.domain.model.DomainTeachingPeriod
import com.datavite.distrivite.domain.repository.TeachingPeriodRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TeachingPeriodRepositoryImpl @Inject constructor(
    private val teachingPeriodRemoteDataSource: TeachingPeriodRemoteDataSource,
    private val teachingPeriodLocalDataSource: TeachingPeriodLocalDataSource,
    private val teachingPeriodMapper: TeachingPeriodMapper
) : TeachingPeriodRepository {
    override suspend fun getTeachingPeriodsFlow(): Flow<List<DomainTeachingPeriod>> {
        return teachingPeriodLocalDataSource.getTeachingPeriodsFlow().map { periods ->
            periods.map { teachingPeriodMapper.mapLocalToDomain(it) }
        }
    }

    override suspend fun getAllTeachingPeriods(): List<DomainTeachingPeriod> {
        return teachingPeriodLocalDataSource.getAllTeachingPeriods().map { teachingPeriodMapper.mapLocalToDomain(it) }
    }

    override suspend fun createTeachingPeriod(organization: String, period: DomainTeachingPeriod) {
        try {
            val remotePeriod = teachingPeriodMapper.mapDomainToRemote(period)
            teachingPeriodRemoteDataSource.createTeachingPeriod(organization, remotePeriod)
            val localPeriod = teachingPeriodMapper.mapDomainToLocal(period, SyncType.SYNCED)
            teachingPeriodLocalDataSource.saveTeachingPeriod(localPeriod)
        } catch (e: Exception) {
            val localPeriod = teachingPeriodMapper.mapDomainToLocal(period, SyncType.PENDING_CREATION)
            teachingPeriodLocalDataSource.saveTeachingPeriod(localPeriod)
        }
    }

    override suspend fun updateTeachingPeriod(organization: String, period: DomainTeachingPeriod) {
        try {
            val remotePeriod = teachingPeriodMapper.mapDomainToRemote(period)
            teachingPeriodRemoteDataSource.updateTeachingPeriod(organization, remotePeriod)
            val localPeriod = teachingPeriodMapper.mapDomainToLocal(period, SyncType.SYNCED)
            teachingPeriodLocalDataSource.saveTeachingPeriod(localPeriod)
        } catch (e: Exception) {
            val localPeriod = teachingPeriodMapper.mapDomainToLocal(period, SyncType.PENDING_MODIFICATION)
            teachingPeriodLocalDataSource.saveTeachingPeriod(localPeriod)
        }
    }

    override suspend fun deleteTeachingPeriod(organization: String, periodId: String) {
        try {
            teachingPeriodRemoteDataSource.deleteTeachingPeriod(organization, periodId)
            teachingPeriodLocalDataSource.deleteTeachingPeriod(periodId)
        } catch (e: Exception) {
            teachingPeriodLocalDataSource.markAsPendingDeletion(periodId)
        }
    }

    override suspend fun syncTeachingPeriods(organization: String) {
        // Fetch unSynced periods from the local database
        val unSyncedPeriods = teachingPeriodLocalDataSource.getUnSyncedPeriods()

        // Try to sync them with the server
        for (period in unSyncedPeriods) {
            try {
                when (period.syncType) {
                    SyncType.PENDING_CREATION -> createTeachingPeriod(organization, teachingPeriodMapper.mapLocalToDomain(period))
                    SyncType.PENDING_MODIFICATION -> {
                        // Should implement conflict resolution between remote and local changes
                        try {
                            val domainPeriod =  teachingPeriodMapper.mapLocalToDomain(period)
                            val remotePeriod = teachingPeriodMapper.mapDomainToRemote(domainPeriod)
                            teachingPeriodRemoteDataSource.updateTeachingPeriod(organization, remotePeriod)
                            val localPeriod = teachingPeriodMapper.mapDomainToLocal(domainPeriod, SyncType.SYNCED)
                            teachingPeriodLocalDataSource.saveTeachingPeriod(localPeriod)
                        } catch (e: Exception) {
                            val domainPeriod =  teachingPeriodMapper.mapLocalToDomain(period)
                            val localPeriod = teachingPeriodMapper.mapDomainToLocal(domainPeriod, SyncType.PENDING_MODIFICATION)
                            teachingPeriodLocalDataSource.saveTeachingPeriod(localPeriod)
                        }
                    }
                    SyncType.PENDING_DELETION -> deleteTeachingPeriod(organization, period.id)
                    else -> {}
                }

            } catch (e: Exception) {
                // Handle the sync failure (e.g., log error, retry later)
            }
        }

        fetchLatestRemoteTeachingPeriodsAndUpdateLocalTeachingPeriods(organization)
    }

    private suspend fun fetchLatestRemoteTeachingPeriodsAndUpdateLocalTeachingPeriods(organization: String){
        try {
            val remotePeriods = teachingPeriodRemoteDataSource.getTeachingPeriods(organization)
            val domainPeriods = remotePeriods.map { teachingPeriodMapper.mapRemoteToDomain(it) }
            val localPeriods = domainPeriods.map { teachingPeriodMapper.mapDomainToLocal(it, SyncType.SYNCED) }
            teachingPeriodLocalDataSource.saveTeachingPeriods(localPeriods)
        } catch (e: Exception) {
            // If fetching from remote fails, fallback to local
        }
    }

}