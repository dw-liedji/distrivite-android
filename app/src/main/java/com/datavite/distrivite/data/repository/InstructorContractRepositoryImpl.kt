package com.datavite.distrivite.data.repository

import com.datavite.distrivite.data.mapper.InstructorContractMapper
import com.datavite.distrivite.data.local.SyncType
import com.datavite.distrivite.data.local.datasource.InstructorContractLocalDataSource
import com.datavite.distrivite.data.remote.datasource.InstructorContractRemoteDataSource
import com.datavite.distrivite.domain.model.DomainInstructorContract
import com.datavite.distrivite.domain.repository.InstructorContractRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class InstructorContractRepositoryImpl @Inject constructor(
    private val localDataSource: InstructorContractLocalDataSource,
    private val remoteDataSource: InstructorContractRemoteDataSource,
    private val instructorContractMapper: InstructorContractMapper
) : InstructorContractRepository {

    override suspend fun getDomainInstructorContractById(id: String): DomainInstructorContract? {
        return localDataSource.getLocalInstructorContractById(id)?.let {
            instructorContractMapper.mapLocalToDomain(it)
        }
    }

    override fun searchDomainInstructorContractsFor(searchQuery: String): List<DomainInstructorContract> {
        return localDataSource.getLocalInstructorContractsFor(searchQuery).map {
            instructorContractMapper.mapLocalToDomain(it)
        }
    }

    override suspend fun getDomainInstructorContractsFlow(): Flow<List<DomainInstructorContract>> {
        return localDataSource.getLocalInstructorContractsFlow().map { instructorContracts ->
            instructorContracts.map { instructorContractMapper.mapLocalToDomain(it) }
        }
    }

    override suspend fun createDomainInstructorContract(organization: String, domainInstructorContract: DomainInstructorContract) {
        val localInstructorContract = instructorContractMapper.mapDomainToLocal(domainInstructorContract, SyncType.PENDING_CREATION)
        localDataSource.saveLocalInstructorContract(localInstructorContract) // Save locally first

        // Attempt to sync with remote in the background
        try {
            val remoteInstructorContract = instructorContractMapper.mapDomainToRemote(domainInstructorContract)
            val createdRemoteInstructorContract = remoteDataSource.createRemoteInstructorContract(organization, remoteInstructorContract)
            val createdDomainInstructorContract = instructorContractMapper.mapRemoteToDomain(createdRemoteInstructorContract)
            val syncedLocalInstructorContract = instructorContractMapper.mapDomainToLocal(createdDomainInstructorContract, SyncType.SYNCED)
            localDataSource.saveLocalInstructorContract(syncedLocalInstructorContract)
        } catch (e: Exception) {
            // Log sync failure, but leave the local data in PENDING_CREATION state
        }
    }

    override suspend fun updateDomainInstructorContract(organization: String, domainInstructorContract: DomainInstructorContract) {
        val localInstructorContract = instructorContractMapper.mapDomainToLocal(domainInstructorContract, SyncType.PENDING_MODIFICATION)
        localDataSource.saveLocalInstructorContract(localInstructorContract) // Save locally first

        // Attempt to sync with remote in the background
        try {
            val remoteInstructorContract = instructorContractMapper.mapDomainToRemote(domainInstructorContract)
            val updatedRemoteInstructorContract = remoteDataSource.updateRemoteInstructorContract(organization, remoteInstructorContract)
            val updatedDomainInstructorContract = instructorContractMapper.mapRemoteToDomain(updatedRemoteInstructorContract)
            val syncedLocalInstructorContract = instructorContractMapper.mapDomainToLocal(updatedDomainInstructorContract, SyncType.SYNCED)
            localDataSource.saveLocalInstructorContract(syncedLocalInstructorContract)
        } catch (e: Exception) {
            // Log sync failure, but leave the local data in PENDING_MODIFICATION state
        }
    }

    override suspend fun deleteDomainInstructorContract(organization: String, domainInstructorContract: DomainInstructorContract) {
        localDataSource.markLocalInstructorContractAsPendingDeletion(domainInstructorContract.id) // Mark locally first

        // Attempt to sync with remote in the background
        try {
            val remoteInstructorContract = instructorContractMapper.mapDomainToRemote(domainInstructorContract)
            remoteDataSource.deleteRemoteInstructorContract(organization, remoteInstructorContract)
            localDataSource.deleteLocalInstructorContract(domainInstructorContract.id)
        } catch (e: Exception) {
            // Log sync failure, but leave the local data in PENDING_DELETION state
        }
    }

    override suspend fun syncLocalWithRemoteInstructorContracts(organization: String) {
        val unSyncedInstructorContracts = localDataSource.getUnSyncedLocalInstructorContracts()

        for (instructorContract in unSyncedInstructorContracts) {
            try {
                when (instructorContract.syncType) {
                    SyncType.PENDING_CREATION -> {
                        val domainInstructorContract = instructorContractMapper.mapLocalToDomain(instructorContract)
                        createDomainInstructorContract(organization, domainInstructorContract)
                    }
                    SyncType.PENDING_MODIFICATION -> {
                        val domainInstructorContract = instructorContractMapper.mapLocalToDomain(instructorContract)
                        updateDomainInstructorContract(organization, domainInstructorContract)
                    }
                    SyncType.PENDING_DELETION -> {
                        val domainInstructorContract = instructorContractMapper.mapLocalToDomain(instructorContract)
                        deleteDomainInstructorContract(organization, domainInstructorContract)
                    }
                    else -> {}
                }
            } catch (e: Exception) {
                // Log sync failure
            }
        }

        fetchLatestRemoteInstructorContractsAndUpdateLocalInstructorContracts(organization)
    }

    private suspend fun fetchLatestRemoteInstructorContractsAndUpdateLocalInstructorContracts(organization: String) {
        try {
            val remoteInstructorContracts = remoteDataSource.getRemoteInstructorContracts(organization)
            val domainInstructorContracts = remoteInstructorContracts.map { instructorContractMapper.mapRemoteToDomain(it) }
            val localInstructorContracts = domainInstructorContracts.map { instructorContractMapper.mapDomainToLocal(it, SyncType.SYNCED) }
            localDataSource.saveLocalInstructorContracts(localInstructorContracts)
        } catch (e: Exception) {
            // Log error if fetching remote contracts fails
        }
    }
}
