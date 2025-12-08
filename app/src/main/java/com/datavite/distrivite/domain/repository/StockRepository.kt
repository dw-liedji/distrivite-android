package com.datavite.distrivite.domain.repository

import FilterOption
import com.datavite.distrivite.domain.model.DomainStock
import kotlinx.coroutines.flow.Flow

interface StockRepository {
    suspend fun getDomainStocksFlow(): Flow<List<DomainStock>>
    suspend fun getDomainStocksFor(searchQuery:String, filterOption: FilterOption): List<DomainStock>
    suspend fun getDomainStocksForFilterOption(filterOption: FilterOption): List<DomainStock>
    suspend fun getDomainStockById(domainStockId:String): DomainStock?
    suspend fun createStock(domainStock: DomainStock)
    suspend fun deleteStock(domainStock: DomainStock)
    suspend fun fetchIfEmpty(organization: String)
}