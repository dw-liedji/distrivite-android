package com.datavite.distrivite.domain.repository

import FilterOption
import com.datavite.distrivite.domain.model.DomainBilling
import kotlinx.coroutines.flow.Flow

interface BillingRepository {
    suspend fun getDomainBillingsFlow(): Flow<List<DomainBilling>>
    suspend fun getDomainBillingsFor(searchQuery:String, filterOption: FilterOption): List<DomainBilling>
    suspend fun getDomainBillingsForFilterOption(filterOption: FilterOption): List<DomainBilling>
    suspend fun getDomainBillingById(domainBillingId:String): DomainBilling?
    suspend fun createBilling(domainBilling: DomainBilling)
    suspend fun updateBilling(domainBilling: DomainBilling)
    suspend fun deliverBilling(domainBilling: DomainBilling)
    suspend fun deleteBilling(domainBilling: DomainBilling)
    suspend fun fetchIfEmpty(organization: String)
}