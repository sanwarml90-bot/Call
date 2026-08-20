package com.smartdialer.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CallLog
import androidx.core.content.ContextCompat
import java.text.DateFormat
import java.util.Date

data class RecentCall(val number: String, val cachedName: String?, val typeLabel: String, val whenLabel: String, val durationSeconds: Long)

class CallLogRepository(private val context: Context) {
    fun hasPermission(): Boolean = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) == PackageManager.PERMISSION_GRANTED

    fun recentCalls(limit: Int = 100): List<RecentCall> {
        if (!hasPermission()) return emptyList()
        val rows = mutableListOf<RecentCall>()
        val projection = arrayOf(CallLog.Calls.NUMBER, CallLog.Calls.CACHED_NAME, CallLog.Calls.TYPE, CallLog.Calls.DATE, CallLog.Calls.DURATION)
        context.contentResolver.query(CallLog.Calls.CONTENT_URI, projection, null, null, "${CallLog.Calls.DATE} DESC")?.use { cursor ->
            val number = cursor.getColumnIndexOrThrow(CallLog.Calls.NUMBER)
            val name = cursor.getColumnIndexOrThrow(CallLog.Calls.CACHED_NAME)
            val type = cursor.getColumnIndexOrThrow(CallLog.Calls.TYPE)
            val date = cursor.getColumnIndexOrThrow(CallLog.Calls.DATE)
            val duration = cursor.getColumnIndexOrThrow(CallLog.Calls.DURATION)
            while (cursor.moveToNext() && rows.size < limit) rows += RecentCall(cursor.getString(number).orEmpty(), cursor.getString(name), typeLabel(cursor.getInt(type)), DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(Date(cursor.getLong(date))), cursor.getLong(duration))
        }
        return rows
    }

    private fun typeLabel(type: Int): String = when (type) {
        CallLog.Calls.INCOMING_TYPE -> "Incoming"
        CallLog.Calls.OUTGOING_TYPE -> "Outgoing"
        CallLog.Calls.MISSED_TYPE -> "Missed"
        CallLog.Calls.REJECTED_TYPE -> "Rejected"
        CallLog.Calls.BLOCKED_TYPE -> "Blocked"
        else -> "Call"
    }
}
