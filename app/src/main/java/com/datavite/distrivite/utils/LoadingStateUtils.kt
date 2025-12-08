package com.datavite.distrivite.utils


import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

/**
 * Extension to safely handle loading state for suspend functions.
 */
suspend fun <T> MutableStateFlow<Boolean>.withLoading(block: suspend () -> T): T {
    value = true
    return try {
        block()
    } finally {
        value = false
    }
}

/**
 * Launches a coroutine that manages loading state automatically.
 */
inline fun <T> MutableStateFlow<Boolean>.launchLoading(
    scope: CoroutineScope,
    crossinline block: suspend () -> T
) {
    scope.launch {
        withLoading { block() }
    }
}
