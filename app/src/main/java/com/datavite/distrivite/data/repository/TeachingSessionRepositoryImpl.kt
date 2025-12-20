package com.datavite.distrivite.data.repository

import FilterOption
import android.database.sqlite.SQLiteConstraintException
import android.util.Log
import com.datavite.distrivite.data.local.dao.PendingNotificationDao
import com.datavite.distrivite.data.local.dao.PendingOperationDao
import com.datavite.distrivite.data.local.datasource.TeachingSessionLocalDataSource
import com.datavite.distrivite.data.local.model.PendingNotificationAction
import com.datavite.distrivite.data.local.model.PendingOperation
import com.datavite.distrivite.data.local.model.SyncStatus
import com.datavite.distrivite.data.mapper.TeachingSessionMapper
import com.datavite.distrivite.data.remote.datasource.TeachingSessionRemoteDataSource
import com.datavite.distrivite.data.sync.EntityType
import com.datavite.distrivite.data.sync.OperationScope
import com.datavite.distrivite.data.sync.OperationType
import com.datavite.distrivite.domain.model.DomainTeachingSession
import com.datavite.distrivite.domain.notification.NotificationBus
import com.datavite.distrivite.domain.notification.NotificationEvent
import com.datavite.distrivite.domain.repository.TeachingSessionRepository
import com.datavite.distrivite.utils.JsonConverter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class TeachingSessionRepositoryImpl @Inject constructor(
    private val localDataSource: TeachingSessionLocalDataSource,
    private val remoteDataSource: TeachingSessionRemoteDataSource,
    private val teachingSessionMapper: TeachingSessionMapper,
    private val pendingOperationDao: PendingOperationDao,
    private val pendingNotificationDao: PendingNotificationDao,
    private val notificationBus: NotificationBus
) : TeachingSessionRepository {

    override suspend fun getTeachingSessionsAsFlow(): Flow<List<DomainTeachingSession>> {
        return localDataSource.getLocalTeachingSessionsFlow().map { sessions -> sessions.map { teachingSessionMapper.mapLocalToDomain(it) } }
    }

    override suspend fun getTeachingSessionsFor(searchQuery: String, filterOption: FilterOption): List<DomainTeachingSession> {
        return localDataSource.getSearchLocalTeachingSessionsFor(searchQuery, filterOption)
            .map { teachingSessionMapper.mapLocalToDomain(it) }
    }

    override suspend fun getTeachingSessionsForFilterOption(filterOption: FilterOption): List<DomainTeachingSession> {
        return localDataSource.getLocalTeachingSessionsForFilterOption(filterOption)
            .map { teachingSessionMapper.mapLocalToDomain(it) }
    }

    override suspend fun getTeachingSessionById(sessionId: String): DomainTeachingSession? {
        val session = localDataSource.getLocalTeachingSessionById(sessionId)
        return session?.let { teachingSessionMapper.mapLocalToDomain(session) }
    }

    override suspend fun createTeachingSession(domainTeachingSession: DomainTeachingSession) {

        val pendingDomainTeachingSession = domainTeachingSession.copy(
            created = LocalDateTime.now().toString(),
            modified = LocalDateTime.now().toString(),
            syncStatus = SyncStatus.PENDING
        )

        val local = teachingSessionMapper.mapDomainToLocal(pendingDomainTeachingSession)
        val remote = teachingSessionMapper.mapDomainToRemote(pendingDomainTeachingSession)

        val operation = PendingOperation(
            orgSlug = domainTeachingSession.orgSlug,
            orgId = domainTeachingSession.orgId,
            entityId = domainTeachingSession.id,
            entityType = EntityType.Session,
            operationType = OperationType.CREATE,
            operationScope = OperationScope.STATE,
            payloadJson = JsonConverter.toJson(remote),
        )

        try {
            localDataSource.insertTeachingSession(local)
            pendingOperationDao.upsertPendingOperation(operation)
            notificationBus.emit(NotificationEvent.Success("Session created successfully"))
        }catch (e: SQLiteConstraintException) {
            notificationBus.emit(NotificationEvent.Failure("Another session with the same period already exists"))
        }

    }

    private suspend fun updateTeachingSession(domainTeachingSession: DomainTeachingSession) {
        val pendingDomainTeachingSession = domainTeachingSession.copy(syncStatus = SyncStatus.PENDING)
        val local = teachingSessionMapper.mapDomainToLocal(pendingDomainTeachingSession)
        val remote = teachingSessionMapper.mapDomainToRemote(pendingDomainTeachingSession)

        val operation = PendingOperation(
            orgSlug = domainTeachingSession.orgSlug,
            orgId = domainTeachingSession.orgId,
            entityId = domainTeachingSession.id,
            entityType = EntityType.Session,
            operationType = OperationType.UPDATE,
            operationScope = OperationScope.STATE,
            payloadJson = JsonConverter.toJson(remote),
        )

        localDataSource.saveLocalTeachingSession(local)
        pendingOperationDao.upsertPendingOperation(operation)
    }

    override suspend fun deleteTeachingSession(domainTeachingSession: DomainTeachingSession) {

        val remote = teachingSessionMapper.mapDomainToRemote(domainTeachingSession)
        val operation = PendingOperation(
            orgSlug = domainTeachingSession.orgSlug,
            orgId = domainTeachingSession.orgId,
            entityId = domainTeachingSession.id,
            entityType = EntityType.Session,
            operationType = OperationType.DELETE,
            operationScope = OperationScope.STATE,
            payloadJson = JsonConverter.toJson(remote),
        )

        localDataSource.deleteLocalTeachingSession(domainTeachingSession.id)
        pendingOperationDao.upsertPendingOperation(operation)

    }

    override suspend fun approve(domainTeachingSession: DomainTeachingSession) {


        val pendingDomainTeachingSession = domainTeachingSession.copy(
            status = "Accepted",
            modified = LocalDateTime.now()
                .toLocalTime()
                .format(DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSS"))
                .toString(),
            syncStatus = SyncStatus.PENDING
        )

        val local = teachingSessionMapper.mapDomainToLocal(pendingDomainTeachingSession)
        val remote = teachingSessionMapper.mapDomainToRemote(pendingDomainTeachingSession)

        val operation = PendingOperation(
            orgSlug = domainTeachingSession.orgSlug,
            orgId = domainTeachingSession.orgId,
            entityId = domainTeachingSession.id,
            entityType = EntityType.Session,
            operationType = OperationType.APPROVE_SESSION,
            operationScope = OperationScope.STATE,
            payloadJson = JsonConverter.toJson(remote),
        )

        localDataSource.saveLocalTeachingSession(local)
        pendingOperationDao.upsertPendingOperation(operation)
    }

    override suspend fun start(domainTeachingSession: DomainTeachingSession) {


        val pendingDomainTeachingSession = domainTeachingSession.copy(
            status = "In Progress",
            rStart = LocalDateTime.now()
                .toLocalTime()
                .format(DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSS"))
                .toString(),
            modified = LocalDateTime.now()
                .toLocalTime()
                .format(DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSS"))
                .toString(),
            syncStatus = SyncStatus.PENDING
        )
        val local = teachingSessionMapper.mapDomainToLocal(pendingDomainTeachingSession)
        val remote = teachingSessionMapper.mapDomainToRemote(pendingDomainTeachingSession)

        val operation = PendingOperation(
            orgSlug = domainTeachingSession.orgSlug,
            orgId = domainTeachingSession.orgId,
            entityId = domainTeachingSession.id,
            entityType = EntityType.Session,
            operationType = OperationType.START_SESSION,
            operationScope = OperationScope.STATE,
            payloadJson = JsonConverter.toJson(remote),
        )

        localDataSource.saveLocalTeachingSession(local)
        pendingOperationDao.upsertPendingOperation(operation)

    }

    override suspend fun end(domainTeachingSession: DomainTeachingSession) {

        val pendingDomainTeachingSession = domainTeachingSession.copy(
            status = "Completed",
            rEnd = LocalDateTime.now()
                .toLocalTime()
                .format(DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSS"))
                .toString(),
            modified = LocalDateTime.now().toString(),
            syncStatus = SyncStatus.PENDING
        )

        val local = teachingSessionMapper.mapDomainToLocal(pendingDomainTeachingSession)
        val remote = teachingSessionMapper.mapDomainToRemote(pendingDomainTeachingSession)

        val operation = PendingOperation(
            orgSlug = domainTeachingSession.orgSlug,
            orgId = domainTeachingSession.orgId,
            entityId = domainTeachingSession.id,
            entityType = EntityType.Session,
            operationType = OperationType.END_SESSION,
            operationScope = OperationScope.STATE,
            payloadJson = JsonConverter.toJson(remote),
        )

        localDataSource.saveLocalTeachingSession(local)
        pendingOperationDao.upsertPendingOperation(operation)

        // Check if there's already a notification action for this session
        val existing = pendingNotificationDao.getPendingForSession(domainTeachingSession.id)
        if (existing == null) {
            val notificationAction = PendingNotificationAction(sessionId = domainTeachingSession.id)
            pendingNotificationDao.insert(notificationAction)
            Log.i("ParentNotificationService", "You inserted for session ${domainTeachingSession.id}")
        }else {
            Log.i("ParentNotificationService", "You already have pending notification for session ${domainTeachingSession.id}")
        }

    }

    override suspend fun fetchIfEmpty(organization: String) {
        try {
            if (localDataSource.getLocalTeachingSessionCount() == 0){
                val remoteSessions = remoteDataSource.getRemoteTeachingSessions(organization)
                val domainSessions = remoteSessions.map { teachingSessionMapper.mapRemoteToDomain(it) }
                val localSessions = domainSessions.map { teachingSessionMapper.mapDomainToLocal(it) }
                localDataSource.clear()
                localDataSource.saveLocalTeachingSessions(localSessions)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("TeachingSessionRepository", " error ${e.message}")
        }
    }

    override suspend fun notifyParents(domainTeachingSession: DomainTeachingSession) {
        val localTeachingSession = teachingSessionMapper.mapDomainToLocal(
            domainTeachingSession.copy(parentsNotified = true)
        )
        localDataSource.saveLocalTeachingSession(localTeachingSession)
    }
}
