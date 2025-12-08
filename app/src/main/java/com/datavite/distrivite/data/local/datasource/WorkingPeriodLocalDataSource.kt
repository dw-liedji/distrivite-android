package com.datavite.distrivite.data.local.datasource

import com.datavite.distrivite.data.local.model.LocalWorkingPeriod
import kotlinx.coroutines.flow.Flow

interface WorkingPeriodLocalDataSource {
    suspend fun getSearchWorkingPeriodsFor(searchQuery:String): List<LocalWorkingPeriod>
    suspend fun getWorkingPeriodsByIdsForDay(ids: List<String>, dayId:Int): List<LocalWorkingPeriod>
    suspend fun getWorkingPeriodsByIds(ids: List<String>): List<LocalWorkingPeriod>    suspend fun getWorkingPeriodsFlow(): Flow<List<LocalWorkingPeriod>>
    suspend fun getUnSyncedWorkingPeriods(): List<LocalWorkingPeriod>
    suspend fun saveWorkingPeriods(workingPeriods: List<LocalWorkingPeriod>)
    suspend fun markAsSynced(workingPeriod: LocalWorkingPeriod)
    suspend fun saveWorkingPeriod(workingPeriod: LocalWorkingPeriod)
    suspend fun deleteWorkingPeriod(workingPeriodId: String)
    suspend fun markAsPendingDeletion(workingPeriodId: String)
}