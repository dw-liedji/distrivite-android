package com.datavite.distrivite.data.mapper

import com.datavite.distrivite.data.local.model.LocalBillingPayment
import com.datavite.distrivite.data.local.model.SyncStatus
import com.datavite.distrivite.data.remote.model.RemoteBillingPayment
import com.datavite.distrivite.domain.model.DomainBillingPayment

class BillingPaymentMapper {

    // Remote → Domain
    fun mapRemoteToDomain(remote: RemoteBillingPayment): DomainBillingPayment {
        return DomainBillingPayment(
            id = remote.id,
            created = remote.created,
            modified = remote.modified,
            orgSlug = remote.orgSlug,
            orgId = remote.orgId,
            billingId = remote.billingId,
            orgUserId = remote.orgUserId,
            transactionBroker = remote.transactionBroker,
            amount = remote.amount,
            syncStatus = SyncStatus.SYNCED
        )
    }

    // Domain → Remote
    fun mapDomainToRemote(domain: DomainBillingPayment): RemoteBillingPayment {
        return RemoteBillingPayment(
            id = domain.id,
            created = domain.created,
            modified = domain.modified,
            orgSlug = domain.orgSlug,
            orgId = domain.orgId,
            billingId = domain.billingId,
            orgUserId = domain.orgUserId,
            transactionBroker = domain.transactionBroker,
            amount = domain.amount,
        )
    }

    // Local → Domain
    fun mapLocalToDomain(local: LocalBillingPayment): DomainBillingPayment {
        return DomainBillingPayment(
            id = local.id,
            created = local.created,
            modified = local.modified,
            orgSlug = local.orgSlug,
            orgId = local.orgId,
            billingId = local.billingId,
            orgUserId = local.orgUserId,
            transactionBroker = local.transactionBroker,
            amount = local.amount,
            syncStatus = local.syncStatus
        )
    }

    // Domain → Local
    fun mapDomainToLocal(domain: DomainBillingPayment): LocalBillingPayment {
        return LocalBillingPayment(
            id = domain.id,
            created = domain.created,
            modified = domain.modified,
            orgSlug = domain.orgSlug,
            orgId = domain.orgId,
            orgUserId = domain.orgUserId,
            billingId = domain.billingId,
            transactionBroker = domain.transactionBroker,
            amount = domain.amount,
            syncStatus = domain.syncStatus
        )
    }
}
