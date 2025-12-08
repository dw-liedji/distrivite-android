package com.datavite.distrivite.data.remote.datasource

import com.datavite.distrivite.data.remote.model.RemoteTeachingPeriod
import com.datavite.distrivite.data.remote.service.TeachingPeriodService
import javax.inject.Inject

class TeachingPeriodRemoteDataSourceImpl @Inject constructor(
    private val teachingPeriodService: TeachingPeriodService
) : TeachingPeriodRemoteDataSource {
    override suspend fun getTeachingPeriods(organization: String): List<RemoteTeachingPeriod> {
        return teachingPeriodService.getTeachingPeriods(organization)
    }

    override suspend fun createTeachingPeriod(organization: String, period: RemoteTeachingPeriod) {
        teachingPeriodService.createTeachingPeriod(organization,period)
    }

    override suspend fun updateTeachingPeriod(organization: String, period: RemoteTeachingPeriod) {
        teachingPeriodService.updateTeachingPeriod(organization,period.id, period)
    }

    override suspend fun deleteTeachingPeriod(organization: String, periodId: String) {
        teachingPeriodService.deleteTeachingPeriod(organization, periodId)
    }
}