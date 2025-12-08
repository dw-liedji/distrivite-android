package com.datavite.distrivite.data.remote.datasource

import com.datavite.distrivite.data.remote.model.RemoteTeachingPeriod


interface TeachingPeriodRemoteDataSource {
    suspend fun getTeachingPeriods(organization:String): List<RemoteTeachingPeriod>
    suspend fun createTeachingPeriod(organization:String, period: RemoteTeachingPeriod)
    suspend fun updateTeachingPeriod(organization:String, period: RemoteTeachingPeriod)
    suspend fun deleteTeachingPeriod(organization:String, periodId: String)
}