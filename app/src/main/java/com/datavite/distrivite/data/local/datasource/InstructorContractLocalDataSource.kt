package com.datavite.distrivite.data.local.datasource

import com.datavite.distrivite.data.local.model.LocalInstructorContract
import kotlinx.coroutines.flow.Flow

interface InstructorContractLocalDataSource {
    suspend fun getLocalInstructorContractById(id: String): LocalInstructorContract?

    fun getLocalInstructorContractsFor(searchQuery: String): List<LocalInstructorContract>

    suspend fun getLocalInstructorContractsFlow(): Flow<List<LocalInstructorContract>>
    suspend fun getUnSyncedLocalInstructorContracts(): List<LocalInstructorContract>
    suspend fun saveLocalInstructorContracts(localInstructorContracts: List<LocalInstructorContract>)
    suspend fun markLocalInstructorContractAsSynced(localInstructorContract: LocalInstructorContract)
    suspend fun saveLocalInstructorContract(localInstructorContract: LocalInstructorContract)
    suspend fun deleteLocalInstructorContract(id: String)
    suspend fun markLocalInstructorContractAsPendingDeletion(id: String)
}