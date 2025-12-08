package com.datavite.distrivite.utils

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
enum class TransactionBroker {
    @SerialName("cashier")
    CASHIER,

    @SerialName("orange_money")
    ORANGE_MONEY,

    @SerialName("mtn_mobile_money")
    MTN_MOBILE_MONEY
}
