package com.smartdialer.service

import android.telecom.Call
import android.telecom.CallScreeningService

class SmartCallScreeningService : CallScreeningService() {
    override fun onScreenCall(details: Call.Details) {
        val number = details.handle?.schemeSpecificPart.orEmpty()
        val block = number.startsWith("000")
        respondToCall(details, CallResponse.Builder().setDisallowCall(block).setRejectCall(block).setSilenceCall(!block && number.isBlank()).setSkipCallLog(false).setSkipNotification(false).build())
    }
}
