package com.smartdialer.service

import android.telecom.Call
import android.telecom.CallScreeningService
import com.smartdialer.domain.RuleStore

/** Uses Android's official screening contract; it never intercepts notifications or uses hidden APIs. */
class SmartCallScreeningService : CallScreeningService() {
    override fun onScreenCall(details: Call.Details) {
        val number = details.handle?.schemeSpecificPart.orEmpty()
        val blocked = number.startsWith("000")
        val ghostDnd = RuleStore(this).shouldSilence(number)
        respondToCall(
            details,
            CallResponse.Builder()
                .setDisallowCall(blocked)
                .setRejectCall(blocked)
                .setSilenceCall(ghostDnd)
                .setSkipCallLog(false)
                .setSkipNotification(false)
                .build()
        )
    }
}