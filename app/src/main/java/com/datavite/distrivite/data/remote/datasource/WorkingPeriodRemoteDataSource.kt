package com.datavite.distrivite.data.remote.datasource

import com.datavite.distrivite.data.remote.model.RemoteWorkingPeriod

interface WorkingPeriodRemoteDataSource {
    suspend fun getWorkingPeriods(organization:String): List<RemoteWorkingPeriod>
    suspend fun createWorkingPeriod(organization:String, session: RemoteWorkingPeriod)
    suspend fun updateWorkingPeriod(organization:String, session: RemoteWorkingPeriod)
    suspend fun deleteWorkingPeriod(organization:String, sessionId: String)
}