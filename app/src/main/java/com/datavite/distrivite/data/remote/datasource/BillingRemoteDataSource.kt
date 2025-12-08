package com.datavite.distrivite.data.remote.datasource

import com.datavite.distrivite.data.remote.model.RemoteBilling

interface BillingRemoteDataSource {
    suspend fun getRemoteBillings(organization:String): List<RemoteBilling>
    suspend fun getRemoteBillingIds(organization: String): List<String>
    suspend fun getRemoteBillingsChangesSince(organization: String, since: Long): List<RemoteBilling>
    suspend fun createRemoteBilling(organization:String, remoteBilling: RemoteBilling) : RemoteBilling
    suspend fun deliverRemoteBilling(organization:String, remoteBilling: RemoteBilling) : RemoteBilling
    suspend fun updateRemoteBilling(organization:String, remoteBilling: RemoteBilling) : RemoteBilling
    suspend fun deleteRemoteBilling(organization:String, remoteBillingId: String) : RemoteBilling
}