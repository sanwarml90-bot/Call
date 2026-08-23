package com.smartdialer

import com.smartdialer.domain.usecase.CallTimer
import com.smartdialer.domain.usecase.NumberNormalizer
import com.smartdialer.domain.usecase.T9Matcher
import com.smartdialer.ui.dialpad.DialPadController
import org.junit.Assert.*
import org.junit.Test

class DomainTest {
    @Test fun t9MatchesHello() { assertTrue(T9Matcher.matches("HELLO", "43556")) }
    @Test fun normalizesNumbers() { assertEquals("+911234567890", NumberNormalizer.normalize(" +91 123-456-7890 ")) }
    @Test fun comparesLastTenDigitsCarefully() { assertTrue(NumberNormalizer.potentiallySame("+911234567890", "1234567890")) }
    @Test fun timerExpiresOnlyWhenActive() { val timer = CallTimer(60); assertFalse(timer.tick(60)); timer.onCallActive(); assertTrue(timer.tick(60)) }
    @Test fun timerDoesNotResetWhenActiveStateRepeats() { val timer = CallTimer(60); timer.onCallActive(); assertFalse(timer.tick(30)); timer.onCallActive(); assertTrue(timer.tick(30)) }
    @Test fun timerIgnoresNonPositiveTicks() { val timer = CallTimer(60); timer.onCallActive(); assertFalse(timer.tick(0)); assertEquals(60, timer.remainingSeconds) }
    @Test fun dialPadAllowsPlusOnlyAsFirstCharacter() { val dialPad = DialPadController(); assertEquals("+", dialPad.append('+')); assertEquals("+1", dialPad.append('1')); assertEquals("+1", dialPad.append('+')) }
}
