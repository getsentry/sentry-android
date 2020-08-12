package io.sentry.logback

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.check
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.sentry.core.ISentryClient
import io.sentry.core.Sentry
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
        val logger: Logger = LoggerFactory.getLogger(SentryAppenderTest::class.java)

        init {
            Sentry.bindClient(client)
        }
    }

    private val fixture = Fixture()

    @Test
    fun `converts message`() {
        fixture.logger.debug("testing message conversion", 1, 2)

        verify(fixture.client).captureEvent(check {
            assertEquals("testing message conversion", it.message.formatted)
            assertEquals(listOf("1", "2"), it.message.params)
            assertEquals("io.sentry.logback.SentryAppenderTest", it.logger)
        }, any(), eq(null))
    }

    @Test
    fun `converts trace log level to Sentry level`() {
        fixture.logger.trace("testing trace level")

        verify(fixture.client).captureEvent(check {
            assertEquals(SentryLevel.DEBUG, it.level)
        }, any(), eq(null))
    }

    @Test
    fun `converts debug log level to Sentry level`() {
        fixture.logger.debug("testing debug level")

        verify(fixture.client).captureEvent(check {
            assertEquals(SentryLevel.DEBUG, it.level)
        }, any(), eq(null))
    }

    @Test
    fun `converts info log level to Sentry level`() {
        fixture.logger.info("testing info level")

        verify(fixture.client).captureEvent(check {
            assertEquals(SentryLevel.INFO, it.level)
        }, any(), eq(null))
    }

    @Test
    fun `converts warn log level to Sentry level`() {
        fixture.logger.warn("testing warn level")

        verify(fixture.client).captureEvent(check {
            assertEquals(SentryLevel.WARNING, it.level)
        }, any(), eq(null))
    }

    @Test
    fun `converts error log level to Sentry level`() {
        fixture.logger.error("testing error level")

        verify(fixture.client).captureEvent(check {
            assertEquals(SentryLevel.ERROR, it.level)
        }, any(), eq(null))
    }

    @Test
    fun `attaches thread information`() {
        fixture.logger.warn("testing thread information")

        verify(fixture.client).captureEvent(check {
            assertNotNull(it.getExtra("thread_name"))
        }, any(), eq(null))
    }

    @Test
    fun `sets tags from MDC`() {
        MDC.put("key", "value")
        fixture.logger.warn("testing thread information")

        verify(fixture.client).captureEvent(check {
            assertEquals("value", it.getTag("key"))
        }, any(), eq(null))
    }

    @Test
    fun `attaches throwable`() {
        val throwable = RuntimeException("something went wrong")
        fixture.logger.warn("testing throwable", throwable)

        verify(fixture.client).captureEvent(check {
            assertEquals(throwable, it.throwable)
        }, any(), eq(null))
    }

    @Test
    @Ignore("sdk version is currently not set")
    fun `sets sdk version name`() {
        fixture.logger.info("testing sdk version")

        verify(fixture.client).captureEvent(check {
            assertEquals(BuildConfig.SENTRY_LOGBACK_SDK_NAME, it.sdk.name)
            assertEquals(BuildConfig.VERSION_NAME, it.sdk.version)
        }, any(), eq(null))
    }
}
