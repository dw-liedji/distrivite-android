package com.datavite.distrivite.data.local.datasource

import FilterOption
import android.database.sqlite.SQLiteConstraintException
import com.datavite.distrivite.data.local.dao.LocalCustomerDao
import com.datavite.distrivite.data.local.model.LocalCustomer
import com.datavite.distrivite.data.local.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class CustomerLocalDataSourceImpl @Inject constructor (
    private val localCustomerDao: LocalCustomerDao,
) : CustomerLocalDataSource {
    override suspend fun getLocalCustomerIds(): List<String> {
        return localCustomerDao.getLocalCustomerIds()
    }

    override suspend fun getLocalCustomersFlow(): Flow<List<LocalCustomer>> {
        return localCustomerDao.getLocalCustomersAsFlow()
    }

    override suspend fun getLocalCustomersFor(
        searchQuery: String,
        filterOption: FilterOption
    ): List<LocalCustomer> {
        TODO("Not yet implemented")
    }

    override suspend fun getLocalCustomersForFilterOption(filterOption: FilterOption): List<LocalCustomer> {
        TODO("Not yet implemented")
    }

    override suspend fun getLocalCustomerById(localCustomerId: String): LocalCustomer? {
        TODO("Not yet implemented")
    }

    override suspend fun insertLocalCustomer(localCustomer: LocalCustomer) {
        try {
            localCustomerDao.insertLocalCustomer(localCustomer)
        }catch (e: SQLiteConstraintException) {
            throw e
        }
    }

    override suspend fun saveLocalCustomer(localCustomer: LocalCustomer) {
        try {
            localCustomerDao.saveLocalCustomer(localCustomer)
        }catch (e: SQLiteConstraintException) {
            throw e
        }
    }

    override suspend fun saveLocalCustomers(localCustomers: List<LocalCustomer>) {
        localCustomerDao.insertOrUpdateLocalCustomers(localCustomers)
    }

    override suspend fun updateSyncStatus(
        id: String,
        syncStatus: SyncStatus
    ) {
        localCustomerDao.updateSyncStatus(id, syncStatus)
    }

    override suspend fun clear() {
        localCustomerDao.clear()
    }

    override suspend fun deleteLocalCustomer(localCustomer: LocalCustomer) {
        localCustomerDao.deleteLocalCustomer(localCustomer.id)
    }

    override suspend fun deleteLocalCustomerById(transactionId: String) {
        localCustomerDao.deleteLocalCustomer(transactionId)
    }

    override suspend fun getLocalCustomerCount(): Int {
        return localCustomerDao.getLocalCustomerCount()
    }

}