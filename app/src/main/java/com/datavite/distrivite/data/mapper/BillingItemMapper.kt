package com.datavite.distrivite.data.mapper

import com.datavite.distrivite.data.local.model.LocalBillingItem
import com.datavite.distrivite.data.local.model.SyncStatus
import com.datavite.distrivite.data.remote.model.RemoteBillingItem
import com.datavite.distrivite.domain.model.DomainBillingItem

class BillingItemMapper {

    // Remote → Domain
    fun mapRemoteToDomain(remote: RemoteBillingItem): DomainBillingItem {
        return DomainBillingItem(
            id = remote.id,
            created = remote.created,
            modified = remote.modified,
            orgSlug = remote.orgSlug,
            orgId = remote.orgId,
            orgUserId = remote.orgUserId,
            isDelivered = remote.isDelivered,
            billingId = remote.billingId,
            stockId = remote.stockId,
            stockName = remote.stockName,
            quantity = remote.quantity,
            unitPrice = remote.unitPrice,
            syncStatus = SyncStatus.SYNCED
        )
    }

    // Domain → Remote
    fun mapDomainToRemote(domain: DomainBillingItem): RemoteBillingItem {
        return RemoteBillingItem(
            id = domain.id,
            created = domain.created,
            modified = domain.modified,
            orgSlug = domain.orgSlug,
            orgId = domain.orgId,
            orgUserId = domain.orgUserId,
            isDelivered = domain.isDelivered,
            billingId = domain.billingId,
            stockId = domain.stockId,
            stockName = domain.stockName,
            quantity = domain.quantity,
            unitPrice = domain.unitPrice,
        )
    }

    // Local → Domain
    fun mapLocalToDomain(local: LocalBillingItem): DomainBillingItem {
        return DomainBillingItem(
            id = local.id,
            created = local.created,
            modified = local.modified,
            orgSlug = local.orgSlug,
            orgId = local.orgId,
            orgUserId = local.orgUserId,
            isDelivered = local.isDelivered,
            billingId = local.billingId,
            stockId = local.stockId,
            stockName = local.stockName,
            quantity = local.quantity,
            unitPrice = local.unitPrice,
            syncStatus = local.syncStatus
        )
    }

    // Domain → Local
    fun mapDomainToLocal(domain: DomainBillingItem): LocalBillingItem {
        return LocalBillingItem(
            id = domain.id,
            created = domain.created,
            modified = domain.modified,
            orgSlug = domain.orgSlug,
            orgId = domain.orgId,
            orgUserId = domain.orgUserId,
            isDelivered = domain.isDelivered,
            billingId = domain.billingId,
            stockId = domain.stockId,
            stockName = domain.stockName,
            quantity = domain.quantity,
            unitPrice = domain.unitPrice,
            syncStatus = domain.syncStatus
        )
    }
}
