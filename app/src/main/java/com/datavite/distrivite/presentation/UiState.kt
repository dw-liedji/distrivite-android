package com.datavite.distrivite.presentation

sealed class UiState<T>(val data: T? = null, val message: String? = null) {
    class Loading<T>(data: T?) : UiState<T>(data)
    class Success<T>(data: T?) : UiState<T>(data)
    class Error<T>(message: String, data: T? = null) : UiState<T>(data, message)
}