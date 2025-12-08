package com.datavite.distrivite.presentation.shopping

import com.datavite.distrivite.domain.model.DomainStock

data class SelectedDomainStock(
    val domainStock: DomainStock,
    var quantity: Int = 1,
    var price: Double = domainStock.billingPrice,
    var isPriceLocked: Boolean = true
) {
    val subtotal: Double get() = quantity * price
}
