package com.datavite.distrivite.data.local.datasource

import com.datavite.distrivite.data.local.dao.EmployeeDao
import com.datavite.distrivite.data.local.model.LocalEmployee
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class EmployeeLocalDataSourceImpl @Inject constructor (
    private val organizationEmployeeDao: EmployeeDao,
) : EmployeeLocalDataSource{
    override suspend fun getEmployeeById(id: String): LocalEmployee? {
        return organizationEmployeeDao.getEmployeeById(id)
    }

    override suspend fun getEmployeesFlow(): Flow<List<LocalEmployee>> {
        return organizationEmployeeDao.getEmployeesFlow()
    }
    override suspend fun getUnSyncedEmployees(): List<LocalEmployee> {
        return organizationEmployeeDao.getUnSyncedEmployees()
    }

    override suspend fun saveEmployees(localEmployees: List<LocalEmployee>) {
        organizationEmployeeDao.insertOrUpdateEmployees(localEmployees)
    }

    override suspend fun markAsSynced(employee: LocalEmployee) {
        organizationEmployeeDao.markEmployeeAsSynced(employee)
    }

    override suspend fun saveEmployee(employee: LocalEmployee) {
        organizationEmployeeDao.saveEmployee(employee)
    }

    override suspend fun deleteEmployee(id: String) {
        organizationEmployeeDao.deleteEmployee(id)
    }

    override suspend fun markAsPendingDeletion(id: String) {
        organizationEmployeeDao.markAsPendingDeletion(id = id)
    }
}