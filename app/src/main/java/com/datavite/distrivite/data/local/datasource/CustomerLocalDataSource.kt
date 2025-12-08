package com.datavite.distrivite.data.local.datasource

import FilterOption
import com.datavite.distrivite.data.local.model.LocalCustomer
import com.datavite.distrivite.data.local.model.SyncStatus
import kotlinx.coroutines.flow.Flow

interface CustomerLocalDataSource {
    suspend fun getLocalCustomerIds(): List<String>
    suspend fun getLocalCustomersFlow(): Flow<List<LocalCustomer>>
    suspend fun getLocalCustomersFor(searchQuery:String, filterOption: FilterOption): List<LocalCustomer>
    suspend fun getLocalCustomersForFilterOption(filterOption: FilterOption): List<LocalCustomer>
    suspend fun getLocalCustomerById(localCustomerId:String): LocalCustomer?
    suspend fun insertLocalCustomer(localCustomer: LocalCustomer)
    suspend fun saveLocalCustomers(localCustomers:List<LocalCustomer>)
    suspend fun saveLocalCustomer(localCustomer: LocalCustomer)
    suspend fun updateSyncStatus(id: String,  syncStatus: SyncStatus)
    suspend fun clear()
    suspend fun deleteLocalCustomer(localCustomer: LocalCustomer)
    suspend fun deleteLocalCustomerById(transactionId: String)
    suspend fun getLocalCustomerCount():Int

}