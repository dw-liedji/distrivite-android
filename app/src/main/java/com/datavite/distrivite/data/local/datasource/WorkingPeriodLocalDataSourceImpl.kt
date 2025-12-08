package com.datavite.distrivite.data.local.datasource

import com.datavite.distrivite.data.local.dao.WorkingPeriodDao
import com.datavite.distrivite.data.local.model.LocalWorkingPeriod
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class WorkingPeriodLocalDataSourceImpl @Inject constructor (
    private val workingPeriodDao: WorkingPeriodDao,
) : WorkingPeriodLocalDataSource{
    override suspend fun getSearchWorkingPeriodsFor(searchQuery:String): List<LocalWorkingPeriod> {
        return if (searchQuery.isEmpty()) workingPeriodDao.getAllWorkingPeriods()
        else workingPeriodDao.getSearchWorkingPeriodsFor(searchQuery)
    }

    override suspend fun getWorkingPeriodsByIdsForDay(ids: List<String>, dayId:Int): List<LocalWorkingPeriod> {
        return workingPeriodDao.getWorkingPeriodsByIdsForDay(ids, dayId)
    }

    override suspend fun getWorkingPeriodsByIds(ids: List<String>): List<LocalWorkingPeriod> {
        return workingPeriodDao.getWorkingPeriodsByIds(ids)
    }
    override suspend fun getWorkingPeriodsFlow(): Flow<List<LocalWorkingPeriod>> {
        return workingPeriodDao.getWorkingPeriodsFlow()
    }
    override suspend fun getUnSyncedWorkingPeriods(): List<LocalWorkingPeriod> {
        return workingPeriodDao.getUnSyncedWorkingPeriods()
    }

    override suspend fun saveWorkingPeriods(workingPeriods: List<LocalWorkingPeriod>) {
        workingPeriodDao.insertOrUpdateWorkingPeriods(workingPeriods)
    }

    override suspend fun markAsSynced(workingPeriod: LocalWorkingPeriod) {
        workingPeriodDao.markWorkingPeriodAsSynced(workingPeriod)
    }

    override suspend fun saveWorkingPeriod(workingPeriod: LocalWorkingPeriod) {
        workingPeriodDao.saveWorkingPeriod(workingPeriod)
    }

    override suspend fun deleteWorkingPeriod(workingPeriodId: String) {
        workingPeriodDao.deleteWorkingPeriod(workingPeriodId)
    }

    override suspend fun markAsPendingDeletion(workingPeriodId: String) {
        workingPeriodDao.markAsPendingDeletion(workingPeriodId = workingPeriodId)
    }
}