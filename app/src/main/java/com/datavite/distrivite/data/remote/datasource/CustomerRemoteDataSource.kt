package com.datavite.distrivite.data.remote.datasource

import com.datavite.distrivite.data.remote.model.RemoteCustomer

interface CustomerRemoteDataSource {

    suspend fun getRemoteCustomerIds(organization: String): List<String>
    suspend fun getRemoteCustomersChangesSince(organization: String, since: Long): List<RemoteCustomer>
    suspend fun getRemoteCustomers(organization:String): List<RemoteCustomer>
    suspend fun createRemoteCustomer(organization:String, remoteCustomer: RemoteCustomer) : RemoteCustomer
    suspend fun updateRemoteCustomer(organization:String, remoteCustomer: RemoteCustomer) : RemoteCustomer
    suspend fun deleteRemoteCustomer(organization:String, remoteCustomerId: String) : RemoteCustomer
}