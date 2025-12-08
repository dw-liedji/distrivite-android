package com.datavite.distrivite.data.remote.datasource

import com.datavite.distrivite.data.remote.model.RemoteInstructorContract
import com.datavite.distrivite.data.remote.service.RemoteInstructorContractService
import javax.inject.Inject

class InstructorContractRemoteDataSourceImpl @Inject constructor(
    private val remoteInstructorContractService: RemoteInstructorContractService
) : InstructorContractRemoteDataSource {
    override suspend fun getRemoteInstructorContracts(organization:String): List<RemoteInstructorContract> {
        return remoteInstructorContractService.getRemoteInstructorContracts(organization)
    }

    override suspend fun createRemoteInstructorContract(organization:String, remoteInstructorContract: RemoteInstructorContract) : RemoteInstructorContract {
        TODO("Not yet implemented")
    }

    override suspend fun updateRemoteInstructorContract(organization:String, remoteInstructorContract: RemoteInstructorContract) : RemoteInstructorContract {
        return remoteInstructorContractService.updateRemoteInstructorContract(organization=organization, remoteInstructorContract.id, remoteInstructorContract)
    }

    override suspend fun deleteRemoteInstructorContract(organization:String, remoteInstructorContract: RemoteInstructorContract) : RemoteInstructorContract{
        return remoteInstructorContractService.deleteRemoteInstructorContract(organization, remoteInstructorContract.id, remoteInstructorContract)
    }
}