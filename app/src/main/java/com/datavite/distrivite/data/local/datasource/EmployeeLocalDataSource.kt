package com.datavite.distrivite.data.local.datasource

import com.datavite.distrivite.data.local.model.LocalEmployee
import kotlinx.coroutines.flow.Flow

interface EmployeeLocalDataSource {
    suspend fun getEmployeeById(id: String): LocalEmployee?
    suspend fun getEmployeesFlow(): Flow<List<LocalEmployee>>
    suspend fun getUnSyncedEmployees(): List<LocalEmployee>
    suspend fun saveEmployees(localEmployees: List<LocalEmployee>)
    suspend fun markAsSynced(employee: LocalEmployee)
    suspend fun saveEmployee(employee: LocalEmployee)
    suspend fun deleteEmployee(id: String)
    suspend fun markAsPendingDeletion(id: String)
}