package io.sentry.core

import com.nhaarman.mockitokotlin2.mock
import io.sentry.core.protocol.Message
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class MainEventProcessorTest {
    class Fixture(attachStacktrace: Boolean = false) {
        private val sentryOptions: SentryOptions = SentryOptions().apply {
            dsn = dsnString
            release = "release"
            environment = "environment"
            dist = "dist"
            isAttachStacktrace = attachStacktrace
        }
        fun getSut() = MainEventProcessor(sentryOptions)
    }

    private val fixture = Fixture()

    @Test
    fun `when processing an event from UncaughtExceptionHandlerIntegration, crashed thread is flagged, mechanism added`() {
        val sut = fixture.getSut()

        val crashedThread = Thread.currentThread()
        var event = generateCrashedEvent(crashedThread)
        event = sut.process(event, null)

        assertSame(crashedThread.id, event.exceptions.first().threadId)
        assertTrue(event.threads.first { t -> t.id == crashedThread.id }.isCrashed)
        assertFalse(event.exceptions.first().mechanism.isHandled)
    }

    @Test
    fun `When hint is not Cached, data should be applied`() {
        val sut = fixture.getSut()
        val crashedThread = Thread.currentThread()
        var event = generateCrashedEvent(crashedThread)
        event = sut.process(event, null)

        assertEquals("release", event.release)
        assertEquals("environment", event.environment)
        assertEquals("dist", event.dist)
        assertTrue(event.threads.first { t -> t.id == crashedThread.id }.isCrashed)
    }

    @Test
    fun `data should be applied only if event doesn't have them`() {
        val sut = fixture.getSut()
        var event = generateCrashedEvent()
        event.dist = "eventDist"
        event.environment = "eventEnvironment"
        event.release = "eventRelease"

        event = sut.process(event, null)

        assertEquals("eventRelease", event.release)
        assertEquals("eventEnvironment", event.environment)
        assertEquals("eventDist", event.dist)
    }

    @Test
    fun `When hint is Cached, data should not be applied`() {
        val sut = fixture.getSut()
        val crashedThread = Thread.currentThread()
        var event = generateCrashedEvent(crashedThread)
        event = sut.process(event, CachedEvent())

        assertNull(event.release)
        assertNull(event.environment)
        assertNull(event.threads)
    }

    @Test
    fun `when processing a message and attach stacktrace is disabled, threads should not be set`() {
        val sut = fixture.getSut()

        var event = SentryEvent().apply {
            message = Message().apply {
                formatted = "test"
            }
        }
        event = sut.process(event, null)

        assertNull(event.threads)
    }

    @Test
    fun `when processing a message and attach stacktrace is enabled, threads should be set`() {
        val sut = Fixture(true).getSut()

        var event = SentryEvent().apply {
            message = Message().apply {
                formatted = "test"
            }
        }
        event = sut.process(event, null)

        assertNotNull(event.threads)
    }

    private fun generateCrashedEvent(crashedThread: Thread = Thread.currentThread()) = SentryEvent().apply {
        val mockThrowable = mock<Throwable>()
        val actualThrowable = UncaughtExceptionHandlerIntegration.getUnhandledThrowable(crashedThread, mockThrowable)
        throwable = actualThrowable
    }
}
