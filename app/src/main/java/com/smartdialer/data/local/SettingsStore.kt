package com.smartdialer.data.local

import android.content.Context

class SettingsStore(context: Context) {
    private val prefs = context.getSharedPreferences("smartdialer_settings", Context.MODE_PRIVATE)
    var theme: String
        get() = prefs.getString("theme", "System") ?: "System"
        set(value) = prefs.edit().putString("theme", value).apply()
    var haptics: Boolean
        get() = prefs.getBoolean("haptics", true)
        set(value) = prefs.edit().putBoolean("haptics", value).apply()
    var autoTimerSeconds: Long
        get() = prefs.getLong("auto_timer_seconds", 0L)
        set(value) = prefs.edit().putLong("auto_timer_seconds", value).apply()
    var unknownCallMode: String
        get() = prefs.getString("unknown_call_mode", "Normal") ?: "Normal"
        set(value) = prefs.edit().putString("unknown_call_mode", value).apply()
}
