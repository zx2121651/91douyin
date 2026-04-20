package com.app.douyin.pro.feature.record.ui.vm

import com.app.douyin.pro.feature.record.ui.state.RecordState

class RecordStateMachine(
    initialState: RecordState = RecordState.IDLE,
    private val onStateChanged: (RecordState) -> Unit
) {
    var currentState: RecordState = initialState
        private set

    fun transitionTo(newState: RecordState): Boolean {
        if (isValidTransition(currentState, newState)) {
            currentState = newState
            onStateChanged(newState)
            return true
        }
        return false
    }

    private fun isValidTransition(from: RecordState, to: RecordState): Boolean {
        return when (from) {
            RecordState.IDLE -> to == RecordState.RECORDING || to == RecordState.ERROR
            RecordState.RECORDING -> to == RecordState.PAUSED || to == RecordState.COMPLETED || to == RecordState.ERROR
            RecordState.PAUSED -> to == RecordState.RECORDING || to == RecordState.COMPLETED || to == RecordState.IDLE || to == RecordState.ERROR
            RecordState.COMPLETED -> to == RecordState.IDLE
            RecordState.ERROR -> to == RecordState.IDLE
        }
    }
}
