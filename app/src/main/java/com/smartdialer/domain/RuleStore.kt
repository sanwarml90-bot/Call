package com.smartdialer.domain

import android.content.Context
import com.smartdialer.domain.usecase.NumberNormalizer

/** Local, user-controlled call rules. No numbers leave the device. */
class RuleStore(context: Context) {
    private val prefs = context.getSharedPreferences(NAME, Context.MODE_PRIVATE)

    var ghostDndEnabled: Boolean
        get() = prefs.getBoolean(KEY_ENABLED, false)
        set(value) { prefs.edit().putBoolean(KEY_ENABLED, value).apply() }

    var ghostDndNumber: String
        get() = prefs.getString(KEY_NUMBER, "").orEmpty()
        set(value) { prefs.edit().putString(KEY_NUMBER, NumberNormalizer.normalize(value)).apply() }

    fun shouldSilence(number: String): Boolean =
        ghostDndEnabled && ghostDndNumber.isNotBlank() &&
            NumberNormalizer.potentiallySame(ghostDndNumber, number)

    private companion object {
        const val NAME = "smart_dialer_rules"
        const val KEY_ENABLED = "ghost_dnd_enabled"
        const val KEY_NUMBER = "ghost_dnd_number"
    }
}