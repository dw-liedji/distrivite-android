package com.datavite.distrivite.data.remote.datasource

import com.datavite.distrivite.data.remote.model.RemoteLeave
import com.datavite.distrivite.data.remote.service.LeaveService
import javax.inject.Inject

class LeaveRemoteDataSourceImpl @Inject constructor(
    private val leaveService: LeaveService
) : LeaveRemoteDataSource {
    override suspend fun getLeaves(organization:String): List<RemoteLeave> {
        return leaveService.getLeaves(organization)
    }

    override suspend fun createLeave(organization:String, leave: RemoteLeave) : RemoteLeave {
        return leaveService.createLeave(organization, leave)
    }

    override suspend fun updateLeave(organization:String, leave: RemoteLeave) : RemoteLeave {
        return leaveService.updateLeave(organization=organization, leave.id, leave)
    }

    override suspend fun deleteLeave(organization:String, leave: RemoteLeave) : RemoteLeave{
        return leaveService.deleteLeave(organization, leave.id, leave)
    }
}