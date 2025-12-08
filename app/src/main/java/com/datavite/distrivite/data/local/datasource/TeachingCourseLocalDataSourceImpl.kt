package com.datavite.distrivite.data.local.datasource

import com.datavite.distrivite.data.local.dao.LocalTeachingCourseDao
import com.datavite.distrivite.data.local.model.LocalTeachingCourse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TeachingCourseLocalDataSourceImpl @Inject constructor (
    private val localTeachingCourseDao: LocalTeachingCourseDao,
) : TeachingCourseLocalDataSource {
    override suspend fun getSearchLocalTeachingCoursesFor(searchQuery:String): List<LocalTeachingCourse> {
        return if (searchQuery.isEmpty()) localTeachingCourseDao.getAllLocalTeachingCourses()
        else localTeachingCourseDao.getSearchLocalTeachingCoursesFor(searchQuery)
    }

    override suspend fun getLocalTeachingCoursesFlow(): Flow<List<LocalTeachingCourse>> {
        return localTeachingCourseDao.getLocalTeachingCoursesFlow()
    }

    override suspend fun searchLocalTeachingCourses(searchQuery: String): Flow<List<LocalTeachingCourse>> {
        return if (searchQuery.isEmpty()) localTeachingCourseDao.getLocalTeachingCoursesFlow() else localTeachingCourseDao.searchLocalTeachingCourses(searchQuery)
    }

    override suspend fun getUnSyncedLocalTeachingCourses(): List<LocalTeachingCourse> {
        return localTeachingCourseDao.getUnSyncedLocalTeachingCourses()
    }

    override suspend fun saveLocalTeachingCourses(localTeachingCourses: List<LocalTeachingCourse>) {
        localTeachingCourseDao.insertOrUpdateLocalTeachingCourses(localTeachingCourses)
    }

    override suspend fun markLocalTeachingCourseAsSynced(localTeachingCourse: LocalTeachingCourse) {
        localTeachingCourseDao.markLocalTeachingCourseAsSynced(localTeachingCourse)
    }

    override suspend fun saveLocalTeachingCourse(localTeachingCourse: LocalTeachingCourse) {
        localTeachingCourseDao.saveLocalTeachingCourse(localTeachingCourse)
    }

    override suspend fun deleteLocalTeachingCourse(localTeachingCourseId: String) {
        localTeachingCourseDao.deleteLocalTeachingCourse(localTeachingCourseId)
    }

    override suspend fun markLocalTeachingCourseAsPendingDeletion(localTeachingCourseId: String) {
        localTeachingCourseDao.markLocalTeachingCourseAsPendingDeletion(localTeachingCourseId = localTeachingCourseId)
    }
}