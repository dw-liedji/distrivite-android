package com.datavite.distrivite.domain.repository

import FilterOption
import com.datavite.distrivite.domain.model.DomainTeachingSession
import kotlinx.coroutines.flow.Flow

interface TeachingSessionRepository {
    suspend fun getTeachingSessionsAsFlow(): Flow<List<DomainTeachingSession>>
    suspend fun getTeachingSessionsFor(searchQuery:String, filterOption: FilterOption): List<DomainTeachingSession>
    suspend fun getTeachingSessionsForFilterOption(filterOption: FilterOption): List<DomainTeachingSession>
    suspend fun getTeachingSessionById(sessionId:String): DomainTeachingSession?
    suspend fun createTeachingSession(domainTeachingSession: DomainTeachingSession)
    suspend fun deleteTeachingSession(domainTeachingSession: DomainTeachingSession)
    suspend fun approve(domainTeachingSession: DomainTeachingSession)
    suspend fun start(domainTeachingSession: DomainTeachingSession)
    suspend fun end(domainTeachingSession: DomainTeachingSession)
    suspend fun fetchIfEmpty(organization: String)
    suspend fun notifyParents(domainTeachingSession: DomainTeachingSession)
}