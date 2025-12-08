package com.datavite.distrivite.data.remote.datasource

import com.datavite.distrivite.data.remote.model.RemoteBilling
import com.datavite.distrivite.data.remote.service.RemoteBillingService
import javax.inject.Inject

class BillingRemoteDataSourceImpl @Inject constructor(
    private val remoteBillingService: RemoteBillingService
) : BillingRemoteDataSource {
    override suspend fun getRemoteBillings(organization:String): List<RemoteBilling> {
        return remoteBillingService.getRemoteBillings(organization)
    }

    override suspend fun getRemoteBillingIds(organization: String): List<String> {
        return remoteBillingService.getRemoteBillingIds(organization)
    }

    override suspend fun getRemoteBillingsChangesSince(
        organization: String,
        since: Long
    ): List<RemoteBilling> {
        // Convert Long timestamp to Django datetime string
        return remoteBillingService.getRemoteBillingsChangesSince(organization, since)
    }


    override suspend fun createRemoteBilling(organization:String, remoteBilling: RemoteBilling): RemoteBilling {
        return remoteBillingService.createRemoteBilling(organization, remoteBilling)
    }

    override suspend fun deliverRemoteBilling(
        organization: String,
        remoteBilling: RemoteBilling
    ): RemoteBilling {
        return remoteBillingService.deleverRemoteBilling(organization, remoteBilling.id, remoteBilling)
    }

    override suspend fun updateRemoteBilling(organization:String, remoteBilling: RemoteBilling): RemoteBilling {
        return remoteBillingService.updateRemoteBilling(organization, remoteBilling.id, remoteBilling)
    }

  override suspend fun deleteRemoteBilling(organization:String, remoteBillingId: String): RemoteBilling {
        return remoteBillingService.deleteRemoteBilling(organization, remoteBillingId)
    }
}