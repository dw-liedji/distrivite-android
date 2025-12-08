package com.datavite.distrivite.data.repository

import com.datavite.distrivite.data.mapper.EmployeeMapper
import com.datavite.distrivite.data.local.SyncType
import com.datavite.distrivite.data.local.datasource.EmployeeLocalDataSource
import com.datavite.distrivite.data.remote.datasource.EmployeeRemoteDataSource
import com.datavite.distrivite.domain.model.DomainEmployee
import com.datavite.distrivite.domain.repository.EmployeeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class EmployeeRepositoryImpl @Inject constructor (
    private val localDataSource: EmployeeLocalDataSource,
    private val remoteDataSource: EmployeeRemoteDataSource,
    private val employeeMapper: EmployeeMapper
) : EmployeeRepository {
    override suspend fun getEmployeeById(id: String): DomainEmployee? {
        localDataSource.getEmployeeById(id)?.let {
            return employeeMapper.mapLocalToDomain(it)
        }
        return null
    }

    override suspend fun getEmployeesFlow(): Flow<List<DomainEmployee>> {
        return localDataSource.getEmployeesFlow().map { employees ->
            employees.map { employeeMapper.mapLocalToDomain(it) }
        }
    }

    override suspend fun createEmployee(organization: String, employee: DomainEmployee) {
        try {
            val remoteEmployee = employeeMapper.mapDomainToRemote(employee)
            val createdRemoteEmployee = remoteDataSource.createEmployee(organization, remoteEmployee)
            val createdDomainEmployee = employeeMapper.mapRemoteToDomain(createdRemoteEmployee)
            val createdLocalEmployee = employeeMapper.mapDomainToLocal(createdDomainEmployee, SyncType.SYNCED)
            localDataSource.saveEmployee(createdLocalEmployee)
        } catch (e: Exception) {
            val localEmployee = employeeMapper.mapDomainToLocal(employee, SyncType.PENDING_CREATION)
            localDataSource.saveEmployee(localEmployee)
        }
    }

    override suspend fun updateEmployee(organization: String, employee: DomainEmployee) {
        try {
            val remoteEmployee = employeeMapper.mapDomainToRemote(employee)
            val updatedRemoteEmployee = remoteDataSource.updateEmployee(organization, remoteEmployee)
            val updatedDomainEmployee = employeeMapper.mapRemoteToDomain(updatedRemoteEmployee)
            val updatedLocalEmployee = employeeMapper.mapDomainToLocal(updatedDomainEmployee, SyncType.SYNCED)
            localDataSource.saveEmployee(updatedLocalEmployee)
        } catch (e: Exception) {
            val localEmployee = employeeMapper.mapDomainToLocal(employee, SyncType.PENDING_MODIFICATION)
            localDataSource.saveEmployee(localEmployee)
        }
    }

    override suspend fun deleteEmployee(organization: String, employee: DomainEmployee) {
        try {
            val remoteEmployee = employeeMapper.mapDomainToRemote(employee)
            val deletedEmployee = remoteDataSource.deleteEmployee(organization, remoteEmployee)
            localDataSource.deleteEmployee(deletedEmployee.id)
        } catch (e: Exception) {
            localDataSource.markAsPendingDeletion(employee.id)
        }
    }

    override suspend fun syncEmployees(organization: String) {
        // Fetch unSynced employees from the local database
        val unSyncedEmployees = localDataSource.getUnSyncedEmployees()

        // Try to sync them with the server
        for (employee in unSyncedEmployees) {
            try {
                when (employee.syncType) {
                    SyncType.PENDING_CREATION -> createEmployee(organization, employeeMapper.mapLocalToDomain(employee))
                    SyncType.PENDING_MODIFICATION -> {
                        // Should implement conflict resolution between remote and local changes
                        try {
                            val domainEmployee =  employeeMapper.mapLocalToDomain(employee)
                            val remoteEmployee = employeeMapper.mapDomainToRemote(domainEmployee)
                            remoteDataSource.updateEmployee(organization, remoteEmployee)
                            val localEmployee = employeeMapper.mapDomainToLocal(domainEmployee, SyncType.SYNCED)
                            localDataSource.saveEmployee(localEmployee)
                        } catch (e: Exception) {
                            val domainEmployee =  employeeMapper.mapLocalToDomain(employee)
                            val localEmployee = employeeMapper.mapDomainToLocal(domainEmployee, SyncType.PENDING_MODIFICATION)
                            localDataSource.saveEmployee(localEmployee)
                        }
                    }
                    SyncType.PENDING_DELETION -> deleteEmployee(
                        organization, employeeMapper.mapLocalToDomain(employee)
                    )
                    else -> {}
                }

            } catch (e: Exception) {
                // Handle the sync failure (e.g., log error, retry later)
            }
        }

        fetchLatestRemoteEmployeesAndUpdateLocalEmployees(organization)
    }

    private suspend fun fetchLatestRemoteEmployeesAndUpdateLocalEmployees(organization: String){
        try {
            val remoteEmployees = remoteDataSource.getEmployees(organization)
            val domainEmployees = remoteEmployees.map { employeeMapper.mapRemoteToDomain(it) }
            val localEmployees = domainEmployees.map { employeeMapper.mapDomainToLocal(it, SyncType.SYNCED) }
            localDataSource.saveEmployees(localEmployees)
        } catch (e: Exception) {
            // If fetching from remote fails, fallback to local
        }
    }

}