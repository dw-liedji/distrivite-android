package com.datavite.distrivite.data.remote.datasource

import com.datavite.distrivite.data.remote.model.RemoteTeachingSession
import com.datavite.distrivite.data.remote.service.RemoteTeachingSessionService
import javax.inject.Inject

class TeachingSessionRemoteDataSourceImpl @Inject constructor(
    private val remoteTeachingSessionService: RemoteTeachingSessionService
) : TeachingSessionRemoteDataSource {
    override suspend fun getRemoteTeachingSessions(organization:String): List<RemoteTeachingSession> {
        return remoteTeachingSessionService.getRemoteTeachingSessions(organization)
    }

    override suspend fun createRemoteTeachingSession(organization:String, remoteTeachingSession: RemoteTeachingSession) {
        remoteTeachingSessionService.createRemoteTeachingSession(organization, remoteTeachingSession)
    }

    override suspend fun updateRemoteTeachingSession(organization:String, remoteTeachingSession: RemoteTeachingSession) {
        remoteTeachingSessionService.updateRemoteTeachingSession(organization, remoteTeachingSession.id, remoteTeachingSession)
    }

    override suspend fun startRemoteTeachingSession(
        organization: String,
        remoteTeachingSession: RemoteTeachingSession
    ) {
        remoteTeachingSessionService.startRemoteTeachingSession(organization, remoteTeachingSession.id, remoteTeachingSession)    }

    override suspend fun endRemoteTeachingSession(
        organization: String,
        remoteTeachingSession: RemoteTeachingSession
    ) {
        remoteTeachingSessionService.endRemoteTeachingSession(organization, remoteTeachingSession.id, remoteTeachingSession)    }

    override suspend fun approveRemoteTeachingSession(
        organization: String,
        remoteTeachingSession: RemoteTeachingSession
    ) {
        remoteTeachingSessionService.approveRemoteTeachingSession(organization, remoteTeachingSession.id, remoteTeachingSession)    }

    override suspend fun deleteRemoteTeachingSession(organization:String, remoteTeachingSessionId: String) {
        TODO("Not yet implemented")
    }
}