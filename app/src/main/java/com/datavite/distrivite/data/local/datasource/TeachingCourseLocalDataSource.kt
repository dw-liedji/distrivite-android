package com.datavite.distrivite.data.local.datasource

import com.datavite.distrivite.data.local.model.LocalTeachingCourse
import kotlinx.coroutines.flow.Flow

interface TeachingCourseLocalDataSource {
    suspend fun getSearchLocalTeachingCoursesFor(searchQuery:String): List<LocalTeachingCourse>

    suspend fun getLocalTeachingCoursesFlow(): Flow<List<LocalTeachingCourse>>
    suspend fun searchLocalTeachingCourses(searchQuery: String): Flow<List<LocalTeachingCourse>>
    suspend fun getUnSyncedLocalTeachingCourses(): List<LocalTeachingCourse>
    suspend fun saveLocalTeachingCourses(localTeachingCourses: List<LocalTeachingCourse>)
    suspend fun markLocalTeachingCourseAsSynced(localTeachingCourse: LocalTeachingCourse)
    suspend fun saveLocalTeachingCourse(localTeachingCourse: LocalTeachingCourse)
    suspend fun deleteLocalTeachingCourse(localTeachingCourseId: String)
    suspend fun markLocalTeachingCourseAsPendingDeletion(localTeachingCourseId: String)
}