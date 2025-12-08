package com.datavite.distrivite.domain.repository

import com.datavite.distrivite.domain.model.DomainEmployee
import kotlinx.coroutines.flow.Flow

interface EmployeeRepository {

    suspend fun getEmployeeById(id: String): DomainEmployee?
    suspend fun getEmployeesFlow(): Flow<List<DomainEmployee>>
    suspend fun createEmployee(organization: String, employee: DomainEmployee)
    suspend fun updateEmployee(organization: String, employee: DomainEmployee)
    suspend fun deleteEmployee(organization: String, employee: DomainEmployee)
    suspend fun syncEmployees(organization: String)
}