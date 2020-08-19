package io.sentry.logback

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.check
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.sentry.core.SentryEvent
import io.sentry.core.SentryLevel
import io.sentry.core.transport.ITransport
import io.sentry.core.transport.TransportResult
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import kotlin.test.AfterTest
import kotlin.test.assertTrue

class SentryAppenderTest {
    private class Fixture {
        val transport = mock<ITransport>()
        val logger: Logger = LoggerFactory.getLogger(SentryAppenderTest::class.java)
        val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext

        init {
            whenever(transport.send(any<SentryEvent>())).thenReturn(TransportResult.success())

            val appender = SentryAppender()
            appender.setDsn("http://key@localhost/proj")
            appender.context = loggerContext
            appender.setTransport(transport)

            val rootLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME)
            rootLogger.level = Level.TRACE
            rootLogger.addAppender(appender)

            appender.start()
            loggerContext.start()
        }
    }

    private val fixture = Fixture()

    @AfterTest
    fun `stop logback`() {
        fixture.loggerContext.stop()
    }

    @Test
    fun `converts message`() {
        fixture.logger.debug("testing message conversion {}, {}", 1, 2)

        verify(fixture.transport).send(check { it: SentryEvent ->
            assertEquals("testing message conversion 1, 2", it.message.formatted)
            assertEquals(listOf("1", "2"), it.message.params)
            assertEquals("io.sentry.logback.SentryAppenderTest", it.logger)
        })
    }

    @Test
    fun `converts trace log level to Sentry level`() {
        fixture.logger.trace("testing trace level")

        verify(fixture.transport).send(check { it: SentryEvent ->
            assertEquals(SentryLevel.DEBUG, it.level)
        })
    }

    @Test
    fun `converts debug log level to Sentry level`() {
        fixture.logger.debug("testing debug level")

        verify(fixture.transport).send(check { it: SentryEvent ->
            assertEquals(SentryLevel.DEBUG, it.level)
        })
    }

    @Test
    fun `converts info log level to Sentry level`() {
        fixture.logger.info("testing info level")

        verify(fixture.transport).send(check { it: SentryEvent ->
            assertEquals(SentryLevel.INFO, it.level)
        })
    }

    @Test
    fun `converts warn log level to Sentry level`() {
        fixture.logger.warn("testing warn level")

        verify(fixture.transport).send(check { it: SentryEvent ->
            assertEquals(SentryLevel.WARNING, it.level)
        })
    }

    @Test
    fun `converts error log level to Sentry level`() {
        fixture.logger.error("testing error level")

        verify(fixture.transport).send(check { it: SentryEvent ->
            assertEquals(SentryLevel.ERROR, it.level)
        })
    }

    @Test
    fun `attaches thread information`() {
        fixture.logger.warn("testing thread information")

        verify(fixture.transport).send(check { it: SentryEvent ->
            assertNotNull(it.getExtra("thread_name"))
        })
    }

    @Test
    fun `sets tags from MDC`() {
        MDC.put("key", "value")
        fixture.logger.warn("testing thread information")

        verify(fixture.transport).send(check { it: SentryEvent ->
            assertEquals(mapOf("key" to "value"), it.contexts["MDC"])
        })
    }

    @Test
    fun `attaches throwable`() {
        val throwable = RuntimeException("something went wrong")
        fixture.logger.warn("testing throwable", throwable)

        verify(fixture.transport).send(check { it: SentryEvent ->
            assertEquals(throwable, it.throwable)
        })
    }

    @Test
    fun `sets SDK version`() {
        fixture.logger.info("testing sdk version")

        verify(fixture.transport).send(check { it: SentryEvent ->
            assertEquals(BuildConfig.SENTRY_LOGBACK_SDK_NAME, it.sdk.name)
            assertEquals(BuildConfig.VERSION_NAME, it.sdk.version)
            assertNotNull(it.sdk.packages)
            assertTrue(it.sdk.packages!!.any { pkg ->
                "maven:sentry-logback" == pkg.name
                    && BuildConfig.VERSION_NAME == pkg.version
            })
        })
    }
}
