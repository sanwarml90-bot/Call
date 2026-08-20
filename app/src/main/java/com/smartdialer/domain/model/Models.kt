package com.smartdialer.domain.model

enum class RuleAction { Normal, Silent, Block, Private, AutoReject, AutoTimer }
data class ContactRule(val phoneNumber: String, val action: RuleAction, val timerSeconds: Long? = null)
data class BlockRule(val pattern: String, val prefix: Boolean = false)
data class AllowRule(val phoneNumber: String)
data class PrivateContact(val displayName: String, val phoneNumber: String, val notes: String = "")
data class CallAnalytics(val totalCalls: Int, val incoming: Int, val outgoing: Int, val missed: Int, val blocked: Int, val talkTimeSeconds: Long)
