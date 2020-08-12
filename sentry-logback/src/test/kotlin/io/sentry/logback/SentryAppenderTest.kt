package io.sentry.logback

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.sentry.core.ISentryClient
import io.sentry.core.Sentry
import io.sentry.core.SentryEvent
import io.sentry.core.SentryLevel
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC

class SentryAppenderTest {
    private class Fixture {
        val client = mock<ISentryClient>()
        val captor = argumentCaptor<SentryEvent>()
        val logger: Logger = LoggerFactory.getLogger(SentryAppenderTest::class.java)

        init {
            Sentry.bindClient(client)
        }
    }

    private val fixture = Fixture()

    @Test
    fun `converts message`() {
        fixture.logger.debug("testing message conversion", 1, 2)

        verify(fixture.client).captureEvent(fixture.captor.capture(), any(), eq(null))

        with(fixture.captor.firstValue) {
            assertEquals("testing message conversion", this.message.formatted)
            assertEquals(listOf("1", "2"), this.message.params)
            assertEquals("io.sentry.logback.SentryAppenderTest", this.logger)
        }
    }

    @Test
    fun `converts trace log level to Sentry level`() {
        fixture.logger.trace("testing trace level")

        verify(fixture.client).captureEvent(fixture.captor.capture(), any(), eq(null))

        with(fixture.captor.firstValue) {
            assertEquals(SentryLevel.DEBUG, this.level)
        }
    }

    @Test
    fun `converts debug log level to Sentry level`() {
        fixture.logger.debug("testing debug level")

        verify(fixture.client).captureEvent(fixture.captor.capture(), any(), eq(null))

        with(fixture.captor.firstValue) {
            assertEquals(SentryLevel.DEBUG, this.level)
        }
    }

    @Test
    fun `converts info log level to Sentry level`() {
        fixture.logger.info("testing info level")

        verify(fixture.client).captureEvent(fixture.captor.capture(), any(), eq(null))

        with(fixture.captor.firstValue) {
            assertEquals(SentryLevel.INFO, this.level)
        }
    }

    @Test
    fun `converts warn log level to Sentry level`() {
        fixture.logger.warn("testing warn level")

        verify(fixture.client).captureEvent(fixture.captor.capture(), any(), eq(null))

        with(fixture.captor.firstValue) {
            assertEquals(SentryLevel.WARNING, this.level)
        }
    }

    @Test
    fun `converts error log level to Sentry level`() {
        fixture.logger.error("testing error level")

        verify(fixture.client).captureEvent(fixture.captor.capture(), any(), eq(null))

        with(fixture.captor.firstValue) {
            assertEquals(SentryLevel.ERROR, this.level)
        }
    }

    @Test
    fun `attaches thread information`() {
        fixture.logger.warn("testing thread information")

        verify(fixture.client).captureEvent(fixture.captor.capture(), any(), eq(null))

        with(fixture.captor.firstValue) {
            assertNotNull(this.getExtra("thread_name"))
        }
    }

    @Test
    fun `sets tags from MDC`() {
        MDC.put("key", "value")
        fixture.logger.warn("testing thread information")

        verify(fixture.client).captureEvent(fixture.captor.capture(), any(), eq(null))

        with(fixture.captor.firstValue) {
            assertEquals("value", this.getTag("key"))
        }
    }

    @Test
    fun `attaches throwable`() {
        val throwable = RuntimeException("something went wrong")
        fixture.logger.warn("testing throwable", throwable)

        verify(fixture.client).captureEvent(fixture.captor.capture(), any(), eq(null))

        with(fixture.captor.firstValue) {
            assertEquals(throwable, this.throwable)
        }
    }

    @Test
    @Ignore("sdk version is currently not set")
    fun `sets sdk version name`() {
        fixture.logger.info("testing sdk version")

        verify(fixture.client).captureEvent(fixture.captor.capture(), any(), eq(null))

        with(fixture.captor.firstValue) {
            assertEquals(BuildConfig.SENTRY_LOGBACK_SDK_NAME, this.sdk.name)
            assertEquals(BuildConfig.VERSION_NAME, this.sdk.version)
        }
    }
}
