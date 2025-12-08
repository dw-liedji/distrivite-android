package com.datavite.distrivite.data.remote.datasource

import com.datavite.distrivite.data.remote.model.RemoteStock
import com.datavite.distrivite.data.remote.service.RemoteStockService
import javax.inject.Inject

class StockRemoteDataSourceImpl @Inject constructor(
    private val remoteStockService: RemoteStockService
) : StockRemoteDataSource {
    override suspend fun getRemoteStockIds(organization: String): List<String> {
        return remoteStockService.getRemoteStockIds(organization)
    }

    override suspend fun getRemoteStocksChangesSince(
        organization: String,
        since: Long
    ): List<RemoteStock> {
        return remoteStockService.getRemoteStocksChangesSince(organization, since)
    }

    override suspend fun getRemoteStocks(organization:String): List<RemoteStock> {
        return remoteStockService.getRemoteStocks(organization)
    }

    override suspend fun createRemoteStock(organization:String, remoteStock: RemoteStock): RemoteStock {
        return remoteStockService.createRemoteStock(organization, remoteStock)
    }

    override suspend fun updateRemoteStock(organization:String, remoteStock: RemoteStock): RemoteStock {
        return remoteStockService.updateRemoteStock(organization, remoteStock.id, remoteStock)
    }

  override suspend fun deleteRemoteStock(organization:String, remoteStockId: String): RemoteStock {
        return remoteStockService.deleteRemoteStock(organization, remoteStockId)
    }
}