package com.datavite.distrivite.data.mapper

import com.datavite.distrivite.data.local.model.LocalBulkCreditPayment
import com.datavite.distrivite.data.local.model.SyncStatus
import com.datavite.distrivite.data.remote.model.RemoteBulkCreditPayment
import com.datavite.distrivite.domain.model.DomainBulkCreditPayment
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BulkCreditPaymentMapper @Inject constructor() {

    fun mapRemoteToDomain(remote: RemoteBulkCreditPayment): DomainBulkCreditPayment {
        return DomainBulkCreditPayment(
            id = remote.id,
            created = remote.created,
            modified = remote.modified,
            billNumber = remote.billNumber,
            customerName = remote.customerName,
            customerId = remote.customerId,
            orgId = remote.orgId,
            orgSlug = remote.orgSlug,
            orgUserId = remote.orgUserId,
            orgUserName = remote.orgUserName,
            transactionBroker = remote.transactionBroker,
            amount = remote.amount,
            syncStatus = SyncStatus.SYNCED // Coming from server means it's synced
        )
    }

    fun mapDomainToLocal(domain: DomainBulkCreditPayment): LocalBulkCreditPayment {
        return LocalBulkCreditPayment(
            id = domain.id,
            created = domain.created,
            modified = domain.modified,
            billNumber = domain.billNumber,
            customerId = domain.customerId,
            customerName = domain.customerName,
            orgSlug = domain.orgSlug,
            orgId = domain.orgId,
            orgUserId = domain.orgUserId,
            orgUserName = domain.orgUserName,
            transactionBroker = domain.transactionBroker,
            amount = domain.amount,
            syncStatus = domain.syncStatus
        )
    }

    fun mapLocalToDomain(local: LocalBulkCreditPayment): DomainBulkCreditPayment {
        return DomainBulkCreditPayment(
            id = local.id,
            created = local.created,
            modified = local.modified,
            billNumber = local.billNumber,
            customerId = local.customerId,
            customerName = local.customerName,
            orgId = local.orgId,
            orgSlug = local.orgSlug,
            orgUserId = local.orgUserId,
            orgUserName = local.orgUserName,
            transactionBroker = local.transactionBroker,
            amount = local.amount,
            syncStatus = local.syncStatus
        )
    }

    fun mapDomainToRemote(domain: DomainBulkCreditPayment): RemoteBulkCreditPayment {
        return RemoteBulkCreditPayment(
            id = domain.id,
            created = domain.created,
            modified = domain.modified,
            billNumber = domain.billNumber,
            customerName = domain.customerName,
            customerId = domain.customerId,
            orgSlug = domain.orgSlug,
            orgId = domain.orgId,
            orgUserId = domain.orgUserId,
            orgUserName = domain.orgUserName,
            transactionBroker = domain.transactionBroker,
            amount = domain.amount
        )
    }

    fun mapLocalToRemote(local: LocalBulkCreditPayment): RemoteBulkCreditPayment {
        return RemoteBulkCreditPayment(
            id = local.id,
            created = local.created,
            modified = local.modified,
            customerId = local.customerId,
            customerName = local.customerName,
            billNumber = local.billNumber,
            orgSlug = local.orgSlug,
            orgId = local.orgId,
            orgUserId = local.orgUserId,
            orgUserName = local.orgUserName,
            transactionBroker = local.transactionBroker,
            amount = local.amount
        )
    }

    // Bulk mapping methods
    fun mapRemoteListToDomainList(remoteList: List<RemoteBulkCreditPayment>): List<DomainBulkCreditPayment> {
        return remoteList.map { mapRemoteToDomain(it) }
    }

    fun mapDomainListToLocalList(domainList: List<DomainBulkCreditPayment>): List<LocalBulkCreditPayment> {
        return domainList.map { mapDomainToLocal(it) }
    }

    fun mapLocalListToDomainList(localList: List<LocalBulkCreditPayment>): List<DomainBulkCreditPayment> {
        return localList.map { mapLocalToDomain(it) }
    }

    fun mapDomainListToRemoteList(domainList: List<DomainBulkCreditPayment>): List<RemoteBulkCreditPayment> {
        return domainList.map { mapDomainToRemote(it) }
    }
}