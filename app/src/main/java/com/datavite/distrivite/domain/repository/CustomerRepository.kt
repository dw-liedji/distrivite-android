package com.datavite.distrivite.domain.repository

import FilterOption
import com.datavite.distrivite.domain.model.DomainCustomer
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
    suspend fun getDomainCustomersFlow(): Flow<List<DomainCustomer>>
    suspend fun getDomainCustomersFor(searchQuery:String, filterOption: FilterOption): List<DomainCustomer>
    suspend fun getDomainCustomersForFilterOption(filterOption: FilterOption): List<DomainCustomer>
    suspend fun getDomainCustomerById(domainCustomerId:String): DomainCustomer?
    suspend fun createCustomer(domainCustomer: DomainCustomer)
    suspend fun saveCustomer(domainCustomer: DomainCustomer)
    suspend fun deleteCustomer(domainCustomer: DomainCustomer)
    suspend fun fetchIfEmpty(organization: String)
}