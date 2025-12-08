package com.datavite.distrivite.data.mapper

import com.datavite.distrivite.data.local.model.LocalBilling
import com.datavite.distrivite.data.local.model.LocalBillingWithItemsAndPaymentsRelation
import com.datavite.distrivite.data.local.model.SyncStatus
import com.datavite.distrivite.data.remote.model.RemoteBilling
import com.datavite.distrivite.domain.model.DomainBilling

class BillingMapper(
    private val billingItemMapper: BillingItemMapper,
    private val billingPaymentMapper: BillingPaymentMapper,
) {

    // --- Remote → Domain ---
    fun mapRemoteToDomain(remote: RemoteBilling): DomainBilling {
        return DomainBilling(
            id = remote.id,
            created = remote.created,
            modified = remote.modified,
            orgSlug = remote.orgSlug,
            orgId = remote.orgId,
            orgUserId = remote.orgUserId,
            orgUserName = remote.orgUserName,
            billNumber = remote.billNumber,
            customerId = remote.customerId,
            customerName = remote.customerName,
            customerPhoneNumber = remote.customerPhoneNumber,
            placedAt = remote.placedAt,
            isDelivered = remote.isDelivered,
            items = remote.items.map { billingItemMapper.mapRemoteToDomain(it) },
            payments = remote.payments.map { billingPaymentMapper.mapRemoteToDomain(it) },
            syncStatus = SyncStatus.SYNCED
        )
    }

    // --- Domain → Remote ---
    fun mapDomainToRemote(domain: DomainBilling): RemoteBilling {
        return RemoteBilling(
            id = domain.id,
            created = domain.created,
            modified = domain.modified,
            orgSlug = domain.orgSlug,
            orgId = domain.orgId,
            orgUserId = domain.orgUserId,
            orgUserName = domain.orgUserName,
            billNumber = domain.billNumber,
            customerId = domain.customerId,
            customerName = domain.customerName,
            customerPhoneNumber = domain.customerPhoneNumber,
            placedAt = domain.placedAt,
            isDelivered = domain.isDelivered,
            items = domain.items.map { billingItemMapper.mapDomainToRemote(it) },
            payments = domain.payments.map { billingPaymentMapper.mapDomainToRemote(it) }
        )
    }

    // --- Local → Domain (uses Room relation model) ---
    fun mapLocalWithItemsAndPaymentsRelationToDomain(local: LocalBillingWithItemsAndPaymentsRelation): DomainBilling {
        return DomainBilling(
            id = local.billing.id,
            created = local.billing.created,
            modified = local.billing.modified,
            orgSlug = local.billing.orgSlug,
            orgId = local.billing.orgId,
            orgUserId = local.billing.orgUserId,
            orgUserName = local.billing.orgUserName,
            billNumber = local.billing.billNumber,
            customerId = local.billing.customerId,
            customerName = local.billing.customerName,
            customerPhoneNumber = local.billing.customerPhoneNumber,
            placedAt = local.billing.placedAt,
            isDelivered = local.billing.isDelivered,
            items = local.items.map { billingItemMapper.mapLocalToDomain(it) },
            payments = local.payments.map { billingPaymentMapper.mapLocalToDomain(it) },
            syncStatus = local.billing.syncStatus
        )
    }

    // --- Domain → Local (for saving only the parent) ---
    fun mapDomainToLocal(domain: DomainBilling): LocalBilling {
        return LocalBilling(
            id = domain.id,
            created = domain.created,
            modified = domain.modified,
            orgSlug = domain.orgSlug,
            orgId = domain.orgId,
            orgUserId = domain.orgUserId,
            orgUserName = domain.orgUserName,
            billNumber = domain.billNumber,
            customerId = domain.customerId,
            customerName = domain.customerName,
            customerPhoneNumber = domain.customerPhoneNumber,
            placedAt = domain.placedAt,
            isDelivered = domain.isDelivered,
            syncStatus = domain.syncStatus
        )
    }

    // --- Domain → Local (parent + children) ---
    fun mapDomainToLocalBillingWithItemsAndPaymentsRelation(domain: DomainBilling): LocalBillingWithItemsAndPaymentsRelation {
        val localBilling = mapDomainToLocal(domain)
        val localItems = domain.items.map { billingItemMapper.mapDomainToLocal(it) }
        val localPayments = domain.payments.map { billingPaymentMapper.mapDomainToLocal(it) }
        return LocalBillingWithItemsAndPaymentsRelation(localBilling, localItems, localPayments)
    }
}
