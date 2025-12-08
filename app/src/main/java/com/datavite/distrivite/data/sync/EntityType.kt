package com.datavite.distrivite.data.sync

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


@Serializable
enum class EntityType (val fetchOrder: Int) {
    @SerialName("attendance")
    Attendance(fetchOrder = 6),
    @SerialName("session")
    Session(fetchOrder = 5),
    @SerialName("customer")
    Customer(fetchOrder = 4),
    @SerialName("transaction")
    Transaction(fetchOrder = 3),
    @SerialName("billing")
    Billing(fetchOrder = 2),
    @SerialName("stock")
    Stock(fetchOrder = 1),
}