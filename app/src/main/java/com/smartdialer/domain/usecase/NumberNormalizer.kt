package com.smartdialer.domain.usecase

object NumberNormalizer {
    fun normalize(input: String): String = input.trim().filterIndexed { index, c -> c.isDigit() || (c == '+' && index == 0) }
    fun potentiallySame(a: String, b: String): Boolean { val na = normalize(a); val nb = normalize(b); return na == nb || na.takeLast(10) == nb.takeLast(10) && minOf(na.length, nb.length) >= 10 }
}
