package com.datavite.distrivite.data.sync

enum class OperationScope {
    STATE,   // replace previous (billing, profile, settings)
    EVENT    // append-only (stock, payments, logs)
}
