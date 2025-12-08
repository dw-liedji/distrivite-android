package com.datavite.distrivite.data.remote.datasource

import com.datavite.distrivite.data.remote.model.RemoteWorkingPeriod
import com.datavite.distrivite.data.remote.service.WorkingPeriodService
import javax.inject.Inject

class WorkingPeriodRemoteDataSourceImpl @Inject constructor(
    private val workingPeriodService: WorkingPeriodService
) : WorkingPeriodRemoteDataSource {
    override suspend fun getWorkingPeriods(organization:String): List<RemoteWorkingPeriod> {
        return workingPeriodService.getWorkingPeriods(organization)
    }

    override suspend fun createWorkingPeriod(organization:String, session: RemoteWorkingPeriod) {
        TODO("Not yet implemented")
    }

    override suspend fun updateWorkingPeriod(organization:String, session: RemoteWorkingPeriod) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteWorkingPeriod(organization:String, sessionId: String) {
        TODO("Not yet implemented")
    }
}