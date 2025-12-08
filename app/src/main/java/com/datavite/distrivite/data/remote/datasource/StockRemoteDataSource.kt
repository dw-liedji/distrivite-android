package com.datavite.distrivite.data.remote.datasource

import com.datavite.distrivite.data.remote.model.RemoteStock

interface StockRemoteDataSource {
    suspend fun getRemoteStockIds(organization: String): List<String>
    suspend fun getRemoteStocksChangesSince(organization: String, since: Long): List<RemoteStock>
    suspend fun getRemoteStocks(organization:String): List<RemoteStock>
    suspend fun createRemoteStock(organization:String, remoteStock: RemoteStock) : RemoteStock
    suspend fun updateRemoteStock(organization:String, remoteStock: RemoteStock) : RemoteStock
    suspend fun deleteRemoteStock(organization:String, remoteStockId: String) : RemoteStock
}