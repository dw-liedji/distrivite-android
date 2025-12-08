package com.datavite.distrivite.data.repository

import com.datavite.distrivite.data.mapper.TeachingCourseMapper
import com.datavite.distrivite.data.remote.datasource.TeachingCourseRemoteDataSource
import com.datavite.distrivite.data.local.SyncType
import com.datavite.distrivite.data.local.datasource.TeachingCourseLocalDataSource
import com.datavite.distrivite.domain.model.DomainTeachingCourse
import com.datavite.distrivite.domain.repository.TeachingCourseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class TeachingCourseRepositoryImpl @Inject constructor (
    private val localDataSource: TeachingCourseLocalDataSource,
    private val remoteDataSource: TeachingCourseRemoteDataSource,
    private val teachingCourseMapper: TeachingCourseMapper
) : TeachingCourseRepository {

    override suspend fun getDomainTeachingCoursesFlow(): Flow<List<DomainTeachingCourse>> {
        return localDataSource.getLocalTeachingCoursesFlow()
            .map { courses -> courses.map { teachingCourseMapper.mapLocalToDomain(it) } }
    }

    override suspend fun searchDomainTeachingCourses(searchQuery: String): Flow<List<DomainTeachingCourse>> {
        return localDataSource.searchLocalTeachingCourses(searchQuery)
            .map { courses -> courses.map { teachingCourseMapper.mapLocalToDomain(it) } }
    }

    override suspend fun getDomainTeachingCoursesFor(searchQuery: String): List<DomainTeachingCourse> {
        return localDataSource.getSearchLocalTeachingCoursesFor(searchQuery)
            .map { teachingCourseMapper.mapLocalToDomain(it) }
    }

    override suspend fun createDomainTeachingCourse(organization: String, domainTeachingSession: DomainTeachingCourse) {
        val localCourse = teachingCourseMapper.mapDomainToLocal(domainTeachingSession, SyncType.PENDING_CREATION)
        localDataSource.saveLocalTeachingCourse(localCourse)

        try {
            val remoteCourse = teachingCourseMapper.mapDomainToRemote(domainTeachingSession)
            remoteDataSource.createRemoteTeachingCourse(organization, remoteCourse)
            val syncedCourse = teachingCourseMapper.mapDomainToLocal(domainTeachingSession, SyncType.SYNCED)
            localDataSource.saveLocalTeachingCourse(syncedCourse)
        } catch (e: Exception) {
            // Keep the local course in a PENDING_CREATION state
        }
    }

    override suspend fun updateDomainTeachingCourse(organization: String, domainTeachingSession: DomainTeachingCourse) {
        val localCourse = teachingCourseMapper.mapDomainToLocal(domainTeachingSession, SyncType.PENDING_MODIFICATION)
        localDataSource.saveLocalTeachingCourse(localCourse)

        try {
            val remoteCourse = teachingCourseMapper.mapDomainToRemote(domainTeachingSession)
            remoteDataSource.updateRemoteTeachingCourse(organization, remoteCourse)
            val syncedCourse = teachingCourseMapper.mapDomainToLocal(domainTeachingSession, SyncType.SYNCED)
            localDataSource.saveLocalTeachingCourse(syncedCourse)
        } catch (e: Exception) {
            // Keep the local course in a PENDING_MODIFICATION state
        }
    }

    override suspend fun deleteDomainTeachingCourse(organization: String, domainTeachingSessionId: String) {
        localDataSource.markLocalTeachingCourseAsPendingDeletion(domainTeachingSessionId)

        try {
            remoteDataSource.deleteRemoteTeachingCourse(organization, domainTeachingSessionId)
            localDataSource.deleteLocalTeachingCourse(domainTeachingSessionId)
        } catch (e: Exception) {
            // Keep the course marked for deletion locally, awaiting future sync
        }
    }

    override suspend fun syncDomainTeachingCourses(organization: String) {
        // Fetch unsynced local teaching sessions
        val unSyncedCourses = localDataSource.getUnSyncedLocalTeachingCourses()

        for (course in unSyncedCourses) {
            try {
                when (course.syncType) {
                    SyncType.PENDING_CREATION -> createDomainTeachingCourse(organization, teachingCourseMapper.mapLocalToDomain(course))
                    SyncType.PENDING_MODIFICATION -> updateDomainTeachingCourse(organization, teachingCourseMapper.mapLocalToDomain(course))
                    SyncType.PENDING_DELETION -> deleteDomainTeachingCourse(organization, course.id)
                    else -> { /* Do nothing for SYNCED or other types */ }
                }
            } catch (e: Exception) {
                // Log error or retry later
            }
        }

        // Finally, fetch latest remote teaching sessions to update local data
        fetchLatestRemoteTeachingCoursesAndUpdateLocalTeachingCourses(organization)
    }

    private suspend fun fetchLatestRemoteTeachingCoursesAndUpdateLocalTeachingCourses(organization: String) {
        try {
            val remoteCourses = remoteDataSource.getRemoteTeachingCourses(organization)
            val domainCourses = remoteCourses.map { teachingCourseMapper.mapRemoteToDomain(it) }
            val localCourses = domainCourses.map { teachingCourseMapper.mapDomainToLocal(it, SyncType.SYNCED) }
            localDataSource.saveLocalTeachingCourses(localCourses)
        } catch (e: Exception) {
            // If fetching from remote fails, local data is retained
        }
    }
}
