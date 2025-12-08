package com.datavite.distrivite.data.remote.service

import com.datavite.distrivite.feature.cameis.data.remote.model.RemoteTeachingCourse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface RemoteTeachingCourseService {
    @GET("en/{organization}/api/v2/teaching-courses/")
    suspend fun getRemoteTeachingCourses(
        @Path("organization") organization: String, // Assuming 'modified' is a date field, adjust type accordingly
     ): List<RemoteTeachingCourse>


    @POST("en/{organization}/api/v2/teaching-courses/")
    suspend fun createRemoteTeachingCourse(
        @Path("organization") organization: String,
        @Body domainRemoteTeachingCourse: RemoteTeachingCourse
    ): RemoteTeachingCourse

    @DELETE("en/{organization}/api/v2/teaching-courses/{id}")
    suspend fun deleteRemoteTeachingCourse(
        @Path("organization") organization: String,
        @Path("id") id: Long
    )
}
