package com.datavite.distrivite.presentation.teachingcourse

import com.datavite.distrivite.domain.model.DomainTeachingCourse

sealed class TeachingCoursesUiState {
    data object Loading : TeachingCoursesUiState()
    data class Success(val teachingCourses: List<DomainTeachingCourse>) : TeachingCoursesUiState()
    data class Error(val message: String) : TeachingCoursesUiState()
}
