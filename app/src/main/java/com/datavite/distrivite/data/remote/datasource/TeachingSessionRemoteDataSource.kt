package com.datavite.distrivite.data.remote.datasource

import com.datavite.distrivite.data.remote.model.RemoteTeachingSession

interface TeachingSessionRemoteDataSource {
    suspend fun getRemoteTeachingSessions(organization:String): List<RemoteTeachingSession>
    suspend fun createRemoteTeachingSession(organization:String, remoteTeachingSession: RemoteTeachingSession)
    suspend fun updateRemoteTeachingSession(organization:String, remoteTeachingSession: RemoteTeachingSession)
    suspend fun startRemoteTeachingSession(organization:String, remoteTeachingSession: RemoteTeachingSession)
    suspend fun endRemoteTeachingSession(organization:String, remoteTeachingSession: RemoteTeachingSession)
    suspend fun approveRemoteTeachingSession(organization:String, remoteTeachingSession: RemoteTeachingSession)
    suspend fun deleteRemoteTeachingSession(organization:String, remoteTeachingSessionId: String)
}