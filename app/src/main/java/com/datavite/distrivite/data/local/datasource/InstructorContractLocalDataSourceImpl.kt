package com.datavite.distrivite.data.local.datasource

import com.datavite.distrivite.data.local.dao.LocalInstructorContractDao
import com.datavite.distrivite.data.local.model.LocalInstructorContract
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class InstructorContractLocalDataSourceImpl @Inject constructor (
    private val localInstructorContractDao: LocalInstructorContractDao,
) : InstructorContractLocalDataSource{
    override suspend fun getLocalInstructorContractById(id: String): LocalInstructorContract? {
        return localInstructorContractDao.getLocalInstructorContractById(id)
    }

    override fun getLocalInstructorContractsFor(searchQuery: String): List<LocalInstructorContract> {
        if (searchQuery.isEmpty()) return localInstructorContractDao.getAllLocalInstructorContracts()
        return localInstructorContractDao.getLocalInstructorContractsFor(searchQuery)
    }

    override suspend fun getLocalInstructorContractsFlow(): Flow<List<LocalInstructorContract>> {
        return localInstructorContractDao.getLocalInstructorContractsFlow()
    }
    override suspend fun getUnSyncedLocalInstructorContracts(): List<LocalInstructorContract> {
        return localInstructorContractDao.getUnSyncedLocalInstructorContracts()
    }

    override suspend fun saveLocalInstructorContracts(localInstructorContracts: List<LocalInstructorContract>) {
        localInstructorContractDao.insertOrUpdateLocalInstructorContracts(localInstructorContracts)
    }

    override suspend fun markLocalInstructorContractAsSynced(localInstructorContract: LocalInstructorContract) {
        localInstructorContractDao.markLocalInstructorContractAsSynced(localInstructorContract)
    }

    override suspend fun saveLocalInstructorContract(localInstructorContract: LocalInstructorContract) {
        localInstructorContractDao.saveLocalInstructorContract(localInstructorContract)
    }

    override suspend fun deleteLocalInstructorContract(id: String) {
        localInstructorContractDao.deleteLocalInstructorContract(id)
    }

    override suspend fun markLocalInstructorContractAsPendingDeletion(id: String) {
        localInstructorContractDao.markLocalInstructorContractAsPendingDeletion(id = id)
    }
}