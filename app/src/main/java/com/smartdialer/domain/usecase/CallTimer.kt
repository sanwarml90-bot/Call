package com.smartdialer.domain.usecase

class CallTimer(private val limitSeconds: Long) {
    var remainingSeconds: Long = limitSeconds
        private set
    var active: Boolean = false
        private set

    fun onCallActive() {
        active = true
    }

    fun onHold() {
        active = false
    }

    fun onEnded() {
        active = false
        remainingSeconds = 0
    }

    fun reset() {
        active = false
        remainingSeconds = limitSeconds
    }

    fun tick(seconds: Long = 1): Boolean {
        if (!active || seconds <= 0) return false
        remainingSeconds = (remainingSeconds - seconds).coerceAtLeast(0)
        return remainingSeconds == 0L
    }
}
