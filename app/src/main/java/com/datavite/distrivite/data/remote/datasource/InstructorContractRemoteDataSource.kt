package com.datavite.distrivite.data.remote.datasource

import com.datavite.distrivite.data.remote.model.RemoteInstructorContract

interface InstructorContractRemoteDataSource {
    suspend fun getRemoteInstructorContracts(organization:String): List<RemoteInstructorContract>
    suspend fun createRemoteInstructorContract(organization:String, remoteInstructorContract: RemoteInstructorContract): RemoteInstructorContract
    suspend fun updateRemoteInstructorContract(organization:String, remoteInstructorContract: RemoteInstructorContract) : RemoteInstructorContract
    suspend fun deleteRemoteInstructorContract(organization:String, remoteInstructorContract: RemoteInstructorContract) : RemoteInstructorContract
}