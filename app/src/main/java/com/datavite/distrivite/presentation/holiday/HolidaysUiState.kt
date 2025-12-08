package com.datavite.distrivite.presentation.holiday

import com.datavite.distrivite.domain.model.DomainHoliday

enum class ATTENDANCE_ACTIONS {
    CHECK_IN,
    CHECK_OUT
}
sealed class HolidaysUiState {
    data object Loading : HolidaysUiState()
    data class Success(val holidays: List<DomainHoliday>) : HolidaysUiState()
    data class Error(val message: String) : HolidaysUiState()
}
