package com.app.douyin.pro.feature.record.ui.vm

import com.app.douyin.pro.feature.record.ui.state.RecordState
import org.junit.Assert.*
import org.junit.Test

class RecordStateMachineTest {

    @Test
    fun testInitialState() {
        val stateMachine = RecordStateMachine(RecordState.IDLE) {}
        assertEquals(RecordState.IDLE, stateMachine.currentState)
    }

    @Test
    fun testValidTransitions() {
        var lastState = RecordState.IDLE
        val stateMachine = RecordStateMachine(RecordState.IDLE) { lastState = it }

        assertTrue(stateMachine.transitionTo(RecordState.RECORDING))
        assertEquals(RecordState.RECORDING, stateMachine.currentState)
        assertEquals(RecordState.RECORDING, lastState)

        assertTrue(stateMachine.transitionTo(RecordState.PAUSED))
        assertEquals(RecordState.PAUSED, stateMachine.currentState)

        assertTrue(stateMachine.transitionTo(RecordState.RECORDING))
        assertEquals(RecordState.RECORDING, stateMachine.currentState)

        assertTrue(stateMachine.transitionTo(RecordState.COMPLETED))
        assertEquals(RecordState.COMPLETED, stateMachine.currentState)

        assertTrue(stateMachine.transitionTo(RecordState.IDLE))
        assertEquals(RecordState.IDLE, stateMachine.currentState)
    }

    @Test
    fun testInvalidTransitions() {
        val stateMachine = RecordStateMachine(RecordState.IDLE) {}

        assertFalse(stateMachine.transitionTo(RecordState.PAUSED))
        assertEquals(RecordState.IDLE, stateMachine.currentState)

        stateMachine.transitionTo(RecordState.RECORDING)
        assertFalse(stateMachine.transitionTo(RecordState.IDLE))
        assertEquals(RecordState.RECORDING, stateMachine.currentState)
    }
}
