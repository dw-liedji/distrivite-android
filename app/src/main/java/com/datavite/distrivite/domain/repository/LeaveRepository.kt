package com.datavite.distrivite.domain.repository

import com.datavite.distrivite.domain.model.DomainLeave
import kotlinx.coroutines.flow.Flow

interface LeaveRepository {

    suspend fun getLeaveById(id: String): DomainLeave?
    suspend fun getLeavesFlow(): Flow<List<DomainLeave>>
    suspend fun getLeaveForEmployeeOnDate(employeeId: String, date: String): DomainLeave?
    suspend fun createLeave(organization: String, leave: DomainLeave)
    suspend fun updateLeave(organization: String, leave: DomainLeave)
    suspend fun deleteLeave(organization: String, leave: DomainLeave)
    suspend fun syncLeaves(organization: String)
}