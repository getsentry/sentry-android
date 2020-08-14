package io.sentry.logback

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.nhaarman.mockitokotlin2.check
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.sentry.core.HubAdapter
import io.sentry.core.SentryLevel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC

class SentryAppenderTest {
    private class Fixture {
        val hubAdapter = mock<HubAdapter>()
        val logger: Logger = LoggerFactory.getLogger(SentryAppenderTest::class.java)

        init {
            val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext

            val appender = SentryAppender(hubAdapter)
            appender.context = loggerContext

            val rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME)
            rootLogger.level = Level.TRACE
            rootLogger.addAppender(appender)

            appender.start()
            loggerContext.start()
        }
    }

    private val fixture = Fixture()

    @Test
    fun `converts message`() {
        fixture.logger.debug("testing message conversion", 1, 2)

        verify(fixture.hubAdapter).captureEvent(check {
            assertEquals("testing message conversion", it.message.formatted)
            assertEquals(listOf("1", "2"), it.message.params)
            assertEquals("io.sentry.logback.SentryAppenderTest", it.logger)
        })
    }

    @Test
    fun `converts trace log level to Sentry level`() {
        fixture.logger.trace("testing trace level")

        verify(fixture.hubAdapter).captureEvent(check {
            assertEquals(SentryLevel.DEBUG, it.level)
        })
    }

    @Test
    fun `converts debug log level to Sentry level`() {
        fixture.logger.debug("testing debug level")

        verify(fixture.hubAdapter).captureEvent(check {
            assertEquals(SentryLevel.DEBUG, it.level)
        })
    }

    @Test
    fun `converts info log level to Sentry level`() {
        fixture.logger.info("testing info level")

        verify(fixture.hubAdapter).captureEvent(check {
            assertEquals(SentryLevel.INFO, it.level)
        })
    }

    @Test
    fun `converts warn log level to Sentry level`() {
        fixture.logger.warn("testing warn level")

        verify(fixture.hubAdapter).captureEvent(check {
            assertEquals(SentryLevel.WARNING, it.level)
        })
    }

    @Test
    fun `converts error log level to Sentry level`() {
        fixture.logger.error("testing error level")

        verify(fixture.hubAdapter).captureEvent(check {
            assertEquals(SentryLevel.ERROR, it.level)
        })
    }

    @Test
    fun `attaches thread information`() {
        fixture.logger.warn("testing thread information")

        verify(fixture.hubAdapter).captureEvent(check {
            assertNotNull(it.getExtra("thread_name"))
        })
    }

    @Test
    fun `sets tags from MDC`() {
        MDC.put("key", "value")
        fixture.logger.warn("testing thread information")

        verify(fixture.hubAdapter).captureEvent(check {
            assertEquals("value", it.getExtra("key"))
        })
    }

    @Test
    fun `attaches throwable`() {
        val throwable = RuntimeException("something went wrong")
        fixture.logger.warn("testing throwable", throwable)

        verify(fixture.hubAdapter).captureEvent(check {
            assertEquals(throwable, it.throwable)
        })
    }
}
