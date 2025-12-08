package com.datavite.distrivite.data.remote.datasource

import com.datavite.distrivite.data.remote.model.RemoteLeave

interface LeaveRemoteDataSource {
    suspend fun getLeaves(organization:String): List<RemoteLeave>
    suspend fun createLeave(organization:String, leave: RemoteLeave): RemoteLeave
    suspend fun updateLeave(organization:String, leave: RemoteLeave) : RemoteLeave
    suspend fun deleteLeave(organization:String, leave: RemoteLeave) : RemoteLeave
}