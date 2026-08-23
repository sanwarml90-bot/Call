package com.smartdialer.ui.dialpad

class DialPadController(private var value: String = "") {
    fun append(char: Char): String {
        when {
            char == '+' && value.isEmpty() -> value += char
            char in "0123456789*#" -> value += char
        }
        return value
    }

    fun delete(): String {
        value = value.dropLast(1)
        return value
    }

    fun clear(): String {
        value = ""
        return value
    }
}
