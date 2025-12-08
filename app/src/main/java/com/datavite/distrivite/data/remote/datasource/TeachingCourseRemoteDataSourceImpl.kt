package com.datavite.distrivite.data.remote.datasource

import com.datavite.distrivite.feature.cameis.data.remote.model.RemoteTeachingCourse
import com.datavite.distrivite.data.remote.service.RemoteTeachingCourseService
import javax.inject.Inject

class TeachingCourseRemoteDataSourceImpl @Inject constructor(
    private val remoteTeachingCourseService: RemoteTeachingCourseService
) : TeachingCourseRemoteDataSource {
    override suspend fun getRemoteTeachingCourses(organization:String): List<RemoteTeachingCourse> {
        return remoteTeachingCourseService.getRemoteTeachingCourses(organization)
    }

    override suspend fun createRemoteTeachingCourse(organization:String, remoteTeachingCourse: RemoteTeachingCourse) {
        TODO("Not yet implemented")
    }

    override suspend fun updateRemoteTeachingCourse(organization:String, remoteTeachingCourse: RemoteTeachingCourse) {
        TODO("Not yet implemented")
    }

    override suspend fun deleteRemoteTeachingCourse(organization:String, remoteTeachingCourseId: String) {
        TODO("Not yet implemented")
    }
}