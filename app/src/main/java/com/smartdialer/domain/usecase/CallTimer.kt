package com.smartdialer.domain.usecase

class CallTimer(private val limitSeconds: Long) {
    var remainingSeconds: Long = limitSeconds; private set
    var active: Boolean = false; private set
    fun onCallActive() { active = true; remainingSeconds = limitSeconds }
    fun onHold() { active = false }
    fun onEnded() { active = false; remainingSeconds = 0 }
    fun tick(seconds: Long = 1): Boolean { if (!active) return false; remainingSeconds = (remainingSeconds - seconds).coerceAtLeast(0); return remainingSeconds == 0L }
}
