package io.sentry.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SentryStackTraceFactoryTest {
    private val sut = SentryStackTraceFactory("io.sentry")

    @Test
    fun `when getStackFrames is called passing a valid Array, not empty result`() {
        val stacktraces = Thread.currentThread().stackTrace
        val count = stacktraces.size
        assertEquals(count, sut.getStackFrames(stacktraces).count())
    }

    @Test
    fun `when getStackFrames is called passing null, empty result`() {
        assertEquals(0, sut.getStackFrames(null).count())
    }

    @Test
    fun `when getStackFrames is called passing a valid array, fields should be set`() {
        val element = StackTraceElement("class", "method", "fileName", -2)
        val stacktraces = Array(1) { element }
        val stackFrames = sut.getStackFrames(stacktraces)
        assertEquals("class", stackFrames[0].module)
        assertEquals("method", stackFrames[0].function)
        assertEquals("fileName", stackFrames[0].filename)
        assertEquals(-2, stackFrames[0].lineno)
        assertEquals(true, stackFrames[0].isNative)
    }

    @Test
    fun `when getStackFrames is called passing a valid inAppPrefix, inAppEnabled should be true`() {
        assertTrue(sut.inAppEnabled)
    }

    @Test
    fun `when getStackFrames is called passing an empty inAppPrefix, inAppEnabled should be false`() {
        assertFalse(SentryStackTraceFactory("").inAppEnabled)
    }

    @Test
    fun `when getStackFrames is called passing a null inAppPrefix, inAppEnabled should be false`() {
        assertFalse(SentryStackTraceFactory(null).inAppEnabled)
    }

    @Test
    fun `when getStackFrames is called passing a valid inAppPrefix, inApp should be true if prefix matches it`() {
        val element = StackTraceElement("io.sentry.MyActivity", "method", "fileName", -2)
        val elements = arrayOf(element)
        val sentryElements = sut.getStackFrames(elements)

        assertTrue(sentryElements.first().inApp)
    }

    @Test
    fun `when getStackFrames is called passing a valid inAppPrefix, inApp should be false if prefix doesnt matches it`() {
        val element = StackTraceElement("io.myapp.MyActivity", "method", "fileName", -2)
        val elements = arrayOf(element)
        val sentryElements = sut.getStackFrames(elements)

        assertFalse(sentryElements.first().inApp)
    }
}
