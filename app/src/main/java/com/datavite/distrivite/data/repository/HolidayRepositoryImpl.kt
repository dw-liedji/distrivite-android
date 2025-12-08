package com.datavite.distrivite.data.repository

import com.datavite.distrivite.data.mapper.HolidayMapper
import com.datavite.distrivite.data.local.SyncType
import com.datavite.distrivite.data.local.datasource.HolidayLocalDataSource
import com.datavite.distrivite.data.remote.datasource.HolidayRemoteDataSource
import com.datavite.distrivite.domain.model.DomainHoliday
import com.datavite.distrivite.domain.repository.HolidayRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HolidayRepositoryImpl @Inject constructor (
    private val localDataSource: HolidayLocalDataSource,
    private val remoteDataSource: HolidayRemoteDataSource,
    private val holidayMapper: HolidayMapper
) : HolidayRepository {
    override suspend fun getHolidayById(id: String): DomainHoliday? {
        localDataSource.getHolidayById(id)?.let {
            return holidayMapper.mapLocalToDomain(it)
        }
        return null
    }

    override suspend fun getHolidaysFlow(): Flow<List<DomainHoliday>> {
        return localDataSource.getHolidaysFlow().map { holidays ->
            holidays.map { holidayMapper.mapLocalToDomain(it) }
        }
    }

    override suspend fun getHolidayForDate(date: String): DomainHoliday? {
        localDataSource.getHolidayForDate(date)?.let {
            return holidayMapper.mapLocalToDomain(it)
        }
        return null
    }

    override suspend fun createHoliday(organization: String, holiday: DomainHoliday) {
        try {
            val remoteHoliday = holidayMapper.mapDomainToRemote(holiday)
            val createdRemoteHoliday = remoteDataSource.createHoliday(organization, remoteHoliday)
            val createdDomainHoliday = holidayMapper.mapRemoteToDomain(createdRemoteHoliday)
            val createdLocalHoliday = holidayMapper.mapDomainToLocal(createdDomainHoliday, SyncType.SYNCED)
            localDataSource.saveHoliday(createdLocalHoliday)
        } catch (e: Exception) {
            val localHoliday = holidayMapper.mapDomainToLocal(holiday, SyncType.PENDING_CREATION)
            localDataSource.saveHoliday(localHoliday)
        }
    }

    override suspend fun updateHoliday(organization: String, holiday: DomainHoliday) {
        try {
            val remoteHoliday = holidayMapper.mapDomainToRemote(holiday)
            val updatedRemoteHoliday = remoteDataSource.updateHoliday(organization, remoteHoliday)
            val updatedDomainHoliday = holidayMapper.mapRemoteToDomain(updatedRemoteHoliday)
            val updatedLocalHoliday = holidayMapper.mapDomainToLocal(updatedDomainHoliday, SyncType.SYNCED)
            localDataSource.saveHoliday(updatedLocalHoliday)
        } catch (e: Exception) {
            val localHoliday = holidayMapper.mapDomainToLocal(holiday, SyncType.PENDING_MODIFICATION)
            localDataSource.saveHoliday(localHoliday)
        }
    }

    override suspend fun deleteHoliday(organization: String, holiday: DomainHoliday) {
        try {
            val remoteHoliday = holidayMapper.mapDomainToRemote(holiday)
            val deletedHoliday = remoteDataSource.deleteHoliday(organization, remoteHoliday)
            localDataSource.deleteHoliday(deletedHoliday.id)
        } catch (e: Exception) {
            localDataSource.markAsPendingDeletion(holiday.id)
        }
    }

    override suspend fun syncHolidays(organization: String) {
        // Fetch unSynced holidays from the local database
        val unSyncedHolidays = localDataSource.getUnSyncedHolidays()

        // Try to sync them with the server
        for (holiday in unSyncedHolidays) {
            try {
                when (holiday.syncType) {
                    SyncType.PENDING_CREATION -> createHoliday(organization, holidayMapper.mapLocalToDomain(holiday))
                    SyncType.PENDING_MODIFICATION -> {
                        // Should implement conflict resolution between remote and local changes
                        try {
                            val domainHoliday =  holidayMapper.mapLocalToDomain(holiday)
                            val remoteHoliday = holidayMapper.mapDomainToRemote(domainHoliday)
                            remoteDataSource.updateHoliday(organization, remoteHoliday)
                            val localHoliday = holidayMapper.mapDomainToLocal(domainHoliday, SyncType.SYNCED)
                            localDataSource.saveHoliday(localHoliday)
                        } catch (e: Exception) {
                            val domainHoliday =  holidayMapper.mapLocalToDomain(holiday)
                            val localHoliday = holidayMapper.mapDomainToLocal(domainHoliday, SyncType.PENDING_MODIFICATION)
                            localDataSource.saveHoliday(localHoliday)
                        }
                    }
                    SyncType.PENDING_DELETION -> deleteHoliday(
                        organization, holidayMapper.mapLocalToDomain(holiday)
                    )
                    else -> {}
                }

            } catch (e: Exception) {
                // Handle the sync failure (e.g., log error, retry later)
            }
        }

        fetchLatestRemoteHolidaysAndUpdateLocalHolidays(organization)
    }

    private suspend fun fetchLatestRemoteHolidaysAndUpdateLocalHolidays(organization: String){
        try {
            val remoteHolidays = remoteDataSource.getHolidays(organization)
            val domainHolidays = remoteHolidays.map { holidayMapper.mapRemoteToDomain(it) }
            val localHolidays = domainHolidays.map { holidayMapper.mapDomainToLocal(it, SyncType.SYNCED) }
            localDataSource.saveHolidays(localHolidays)
        } catch (e: Exception) {
            // If fetching from remote fails, fallback to local
        }
    }

}