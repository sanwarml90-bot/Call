package com.smartdialer.service

import android.telecom.Call
import android.telecom.InCallService
import com.smartdialer.domain.usecase.CallTimer

class SmartInCallService : InCallService() {
    private val timers = mutableMapOf<Call, CallTimer>()
    override fun onCallAdded(call: Call) { super.onCallAdded(call); call.registerCallback(callback) }
    override fun onCallRemoved(call: Call) { call.unregisterCallback(callback); timers.remove(call)?.onEnded(); super.onCallRemoved(call) }
    private val callback = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            val timer = timers.getOrPut(call) { CallTimer(600) }
            when (state) { Call.STATE_ACTIVE -> timer.onCallActive(); Call.STATE_HOLDING -> timer.onHold(); Call.STATE_DISCONNECTED -> timer.onEnded() }
            if (timer.tick(0)) call.disconnect()
        }
    }
}
