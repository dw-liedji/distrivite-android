package com.datavite.distrivite.data.remote.service

import com.datavite.distrivite.data.remote.model.RemoteStudentAttendance
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface StudentAttendanceService {
    @GET("en/{organization}/api/v2/student-attendances/")
    suspend fun getAttendances(
        @Path("organization") organization: String, // Assuming 'modified' is a date field, adjust type accordingly
     ): List<RemoteStudentAttendance>

    @POST("en/{organization}/api/v2/student-attendances/create")
    suspend fun createAttendance(
        @Path("organization") organization: String,
        @Body domainAttendance: RemoteStudentAttendance
    ): RemoteStudentAttendance


    @POST("en/{organization}/api/v2/student-attendances/{id}/edit")
    suspend fun updateAttendance(
        @Path("organization") organization: String,
        @Path("id") id: String,
        @Body remoteAttendance: RemoteStudentAttendance
    ): RemoteStudentAttendance

    @DELETE("en/{organization}/api/v2/student-attendances/{id}/delete")
    suspend fun deleteAttendance(
        @Path("organization") organization: String,
        @Path("id") id: String
    )
}
