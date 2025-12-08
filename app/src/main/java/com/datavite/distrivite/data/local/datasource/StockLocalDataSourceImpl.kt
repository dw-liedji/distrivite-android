package com.datavite.distrivite.data.local.datasource

import FilterOption
import com.datavite.distrivite.data.local.dao.LocalStockDao
import com.datavite.distrivite.data.local.model.LocalStock
import com.datavite.distrivite.data.local.model.SyncStatus
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class StockLocalDataSourceImpl @Inject constructor (
    private val localStockDao: LocalStockDao,
) : StockLocalDataSource {
    override suspend fun getLocalStockIds(): List<String> {
        return localStockDao.getLocalStockIds()
    }

    override suspend fun getLocalStocksFlow(): Flow<List<LocalStock>> {
        return localStockDao.getLocalStocksAsFlow()
    }

    override suspend fun getLocalStocksFor(
        searchQuery: String,
        filterOption: FilterOption
    ): List<LocalStock> {
        TODO("Not yet implemented")
    }

    override suspend fun getLocalStocksForFilterOption(filterOption: FilterOption): List<LocalStock> {
        TODO("Not yet implemented")
    }

    override suspend fun getLocalStockById(localStockId: String): LocalStock? {
        TODO("Not yet implemented")
    }

    override suspend fun insertLocalStock(localStock: LocalStock) {
        localStockDao.saveLocalStock(localStock)
    }

    override suspend fun saveLocalStocks(localStocks: List<LocalStock>) {
        localStockDao.insertOrUpdateLocalStocks(localStocks)
    }

    override suspend fun updateSyncStatus(
        id: String,
        syncStatus: SyncStatus
    ) {
        localStockDao.updateSyncStatus(id, syncStatus)
    }

    override suspend fun clear() {
        localStockDao.clear()
    }

    override suspend fun deleteLocalStock(localStock: LocalStock) {
        localStockDao.deleteLocalStock(localStock.id)
    }

    override suspend fun deleteLocalStockById(localStockId: String) {
        localStockDao.deleteLocalStock(localStockId)
    }

    override suspend fun getLocalStockCount(): Int {
        return localStockDao.getLocalStockCount()
    }

}