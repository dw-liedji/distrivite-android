package com.datavite.distrivite.domain.model

import com.datavite.distrivite.data.local.model.SyncStatus

data class DomainBilling(
    val id: String,
    val created: String,
    val modified: String,
    val orgSlug: String,
    val orgId: String,
    val orgUserId: String,
    val orgUserName: String,
    val billNumber: String,
    val placedAt: String,
    val customerId: String,
    val customerName: String,
    val customerPhoneNumber: String?,
    val isDelivered: Boolean,
    val items: List<DomainBillingItem>,
    val payments: List<DomainBillingPayment>,
    val syncStatus: SyncStatus
){
    val totalPrice: Double
        get() = items.sumOf { (it.quantity * it.unitPrice) }
}