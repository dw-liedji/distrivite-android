package com.datavite.distrivite.domain.repository

import com.datavite.distrivite.domain.model.DomainWorkingPeriod
import kotlinx.coroutines.flow.Flow

interface WorkingPeriodRepository {
    suspend fun getWorkingPeriodsFlow(): Flow<List<DomainWorkingPeriod>>
    suspend fun getWorkingPeriodsFor(searchQuery:String): List<DomainWorkingPeriod>
    suspend fun createWorkingPeriod(organization: String, session: DomainWorkingPeriod)
    suspend fun updateWorkingPeriod(organization: String, session: DomainWorkingPeriod)
    suspend fun deleteWorkingPeriod(organization: String, sessionId: String)
    suspend fun getWorkingPeriodsByIdsForDay(ids: List<String>, dayId:Int): List<DomainWorkingPeriod>
    suspend fun getWorkingPeriodsByIds(ids: List<String>): List<DomainWorkingPeriod>
    suspend fun syncWorkingPeriods(organization: String)
}