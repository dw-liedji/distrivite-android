package com.datavite.distrivite.domain.repository

import com.datavite.distrivite.domain.model.DomainTeachingCourse
import kotlinx.coroutines.flow.Flow

interface TeachingCourseRepository {
    suspend fun getDomainTeachingCoursesFlow(): Flow<List<DomainTeachingCourse>>
    suspend fun searchDomainTeachingCourses(searchQuery: String): Flow<List<DomainTeachingCourse>>
    suspend fun getDomainTeachingCoursesFor(searchQuery:String): List<DomainTeachingCourse>
    suspend fun createDomainTeachingCourse(organization: String, domainTeachingSession: DomainTeachingCourse)
    suspend fun updateDomainTeachingCourse(organization: String, domainTeachingSession: DomainTeachingCourse)
    suspend fun deleteDomainTeachingCourse(organization: String, domainTeachingSessionId: String)
    suspend fun syncDomainTeachingCourses(organization: String)
}