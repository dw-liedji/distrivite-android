package com.datavite.distrivite.data.local.datasource

import com.datavite.distrivite.data.local.model.LocalTeachingPeriod
import com.datavite.distrivite.data.local.dao.TeachingPeriodDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TeachingPeriodLocalDataSourceImpl @Inject constructor(
    private val teachingPeriodDao: TeachingPeriodDao
) : TeachingPeriodLocalDataSource {
    override suspend fun getTeachingPeriodsFlow(): Flow<List<LocalTeachingPeriod>> {
        return teachingPeriodDao.getAllTeachingPeriodFlows()
    }

    override suspend fun getAllTeachingPeriods(): List<LocalTeachingPeriod> {
        return teachingPeriodDao.getAllTeachingPeriods()
    }

    override suspend fun saveTeachingPeriods(localPeriods: List<LocalTeachingPeriod>) {
        for (localPeriod in localPeriods) {
            teachingPeriodDao.saveTeachingPeriod(localPeriod)
        }
    }

    override suspend fun getUnSyncedPeriods(): List<LocalTeachingPeriod> {
        return teachingPeriodDao.getUnSyncedPeriods()
    }

    override suspend fun markAsSynced(period: LocalTeachingPeriod) {
        teachingPeriodDao.markPeriodAsSynced(period)
    }

    override suspend fun saveTeachingPeriod(period: LocalTeachingPeriod) {
        teachingPeriodDao.saveTeachingPeriod(period)
    }

    override suspend fun deleteTeachingPeriod(periodId: String) {
        teachingPeriodDao.deleteTeachingPeriod(periodId)
    }

    override suspend fun markAsPendingDeletion(periodId: String) {
        teachingPeriodDao.markAsPendingDeletion(periodId)
    }
}