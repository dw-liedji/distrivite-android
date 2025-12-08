package com.datavite.distrivite.data.remote.datasource

import com.datavite.distrivite.feature.cameis.data.remote.model.RemoteTeachingCourse

interface TeachingCourseRemoteDataSource {
    suspend fun getRemoteTeachingCourses(organization:String): List<RemoteTeachingCourse>
    suspend fun createRemoteTeachingCourse(organization:String, remoteTeachingCourse: RemoteTeachingCourse)
    suspend fun updateRemoteTeachingCourse(organization:String, remoteTeachingCourse: RemoteTeachingCourse)
    suspend fun deleteRemoteTeachingCourse(organization:String, remoteTeachingCourseId: String)
}