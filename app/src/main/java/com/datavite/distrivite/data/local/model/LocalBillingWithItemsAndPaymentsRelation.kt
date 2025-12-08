package com.datavite.distrivite.data.local.model

import androidx.room.Embedded
import androidx.room.Relation

// ✅ FIXED: use @Embedded and parentColumn = "id"
data class LocalBillingWithItemsAndPaymentsRelation(
    @Embedded val billing: LocalBilling,

    @Relation(
        parentColumn = "id",
        entityColumn = "billingId"
    )
    val items: List<LocalBillingItem>,

    @Relation(
        parentColumn = "id",
        entityColumn = "billingId"
    )
    val payments: List<LocalBillingPayment>
)
