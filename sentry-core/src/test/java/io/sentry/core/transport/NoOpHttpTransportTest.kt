package io.sentry.core.transport

import io.sentry.core.SentryEvent
import kotlin.test.Test
import kotlin.test.assertEquals

class NoOpHttpTransportTest {
    private val sut: NoOpHttpTransport = NoOpHttpTransport.getInstance()

    @Test
    fun `send doesn't throw on null params`() {
        sut.send(null)
    }

    @Test
    fun `close doesn't throw on NoOp`() = sut.close()

    @Test
    fun `send returns error on NoOp`() {
        val transportResult = sut.send(SentryEvent())
        assertEquals(-1, transportResult.responseCode)
        assertEquals(-1, transportResult.retryMillis)
        assertEquals(false, transportResult.isSuccess)
    }
}
