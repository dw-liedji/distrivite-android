package com.datavite.distrivite.presentation.session

import com.ramcosta.composedestinations.spec.Direction

sealed class TeachingSessionActionExecutionState {
    data object Started : TeachingSessionActionExecutionState()
    data class Finished(val direction: Direction) : TeachingSessionActionExecutionState()
}