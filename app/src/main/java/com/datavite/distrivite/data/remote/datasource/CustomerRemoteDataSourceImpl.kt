package com.datavite.distrivite.data.remote.datasource

import com.datavite.distrivite.data.remote.model.RemoteCustomer
import com.datavite.distrivite.data.remote.service.RemoteCustomerService
import javax.inject.Inject

class CustomerRemoteDataSourceImpl @Inject constructor(
    private val remoteCustomerService: RemoteCustomerService
) : CustomerRemoteDataSource {
    override suspend fun getRemoteCustomerIds(organization: String): List<String> {
        return remoteCustomerService.getRemoteCustomerIds(organization)
    }

    override suspend fun getRemoteCustomersChangesSince(
        organization: String,
        since: Long
    ): List<RemoteCustomer> {
        return remoteCustomerService.getRemoteCustomersChangesSince(organization, since)
    }

    override suspend fun getRemoteCustomers(organization:String): List<RemoteCustomer> {
        return remoteCustomerService.getRemoteCustomers(organization)
    }

    override suspend fun createRemoteCustomer(organization:String, remoteCustomer: RemoteCustomer): RemoteCustomer {
        return remoteCustomerService.createRemoteCustomer(organization, remoteCustomer)
    }

    override suspend fun updateRemoteCustomer(organization:String, remoteCustomer: RemoteCustomer): RemoteCustomer {
        return remoteCustomerService.updateRemoteCustomer(organization, remoteCustomer.id, remoteCustomer)
    }

  override suspend fun deleteRemoteCustomer(organization:String, remoteCustomerId: String): RemoteCustomer {
        return remoteCustomerService.deleteRemoteCustomer(organization, remoteCustomerId)
    }
}