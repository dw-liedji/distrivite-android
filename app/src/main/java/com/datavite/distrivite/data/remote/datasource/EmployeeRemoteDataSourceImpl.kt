package com.datavite.distrivite.data.remote.datasource

import com.datavite.distrivite.data.remote.model.RemoteEmployee
import com.datavite.distrivite.data.remote.service.EmployeeService
import javax.inject.Inject

class EmployeeRemoteDataSourceImpl @Inject constructor(
    private val organizationUserService: EmployeeService
) : EmployeeRemoteDataSource {
    override suspend fun getEmployees(organization:String): List<RemoteEmployee> {
        return organizationUserService.getEmployees(organization)
    }

    override suspend fun createEmployee(organization:String, employee: RemoteEmployee) : RemoteEmployee {
        TODO("Not yet implemented")
    }

    override suspend fun updateEmployee(organization:String, employee: RemoteEmployee) : RemoteEmployee {
        return organizationUserService.updateEmployee(organization=organization, employee.id, employee)
    }

    override suspend fun deleteEmployee(organization:String, employee: RemoteEmployee) : RemoteEmployee{
        return organizationUserService.deleteEmployee(organization, employee.id, employee)
    }
}