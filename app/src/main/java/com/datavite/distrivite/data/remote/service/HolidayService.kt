package com.datavite.distrivite.data.remote.service

import com.datavite.distrivite.data.remote.model.RemoteHoliday
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path


interface HolidayService {
    @GET("en/{organization}/api/v2/holidays/")
    suspend fun getHolidays(
        @Path("organization") organization: String,
     ): List<RemoteHoliday>

    @POST("en/{organization}/api/v2/holidays/create")
    suspend fun createHoliday(
        @Path("organization") organization: String,
        @Body remoteHoliday: RemoteHoliday
    ): RemoteHoliday

    @PUT("en/{organization}/api/v2/holidays/{id}/edit")
    suspend fun updateHoliday(
            @Path("organization") organization: String,
            @Path("id") id: String,
            @Body remoteHoliday: RemoteHoliday
    ): RemoteHoliday

    @DELETE("en/{organization}/api/v2/holidays/{id}/delete")
    suspend fun deleteHoliday(
        @Path("organization") organization: String,
        @Path("id") id: String,
        @Body remoteHoliday: RemoteHoliday
    ): RemoteHoliday
}
