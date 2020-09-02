package io.sentry.log4j2

import com.nhaarman.mockitokotlin2.check
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.sentry.core.SentryEvent
import io.sentry.core.SentryLevel
import io.sentry.core.transport.ITransport
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.apache.logging.log4j.Level
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.apache.logging.log4j.ThreadContext
import org.apache.logging.log4j.core.LoggerContext
import org.apache.logging.log4j.core.config.AppenderRef
import org.apache.logging.log4j.core.config.Configuration
import org.apache.logging.log4j.core.config.LoggerConfig
import org.awaitility.kotlin.await

class SentryAppenderTest {
    private class Fixture(minimumBreadcrumbLevel: Level? = null, minimumEventLevel: Level? = null) {
        val transport = mock<ITransport>()
        val logger: Logger
        val loggerContext = LogManager.getContext() as LoggerContext

        init {
            loggerContext.start()
            val config: Configuration = loggerContext.configuration
            val appender = SentryAppender("sentry", null, "http://key@localhost/proj", minimumBreadcrumbLevel, minimumEventLevel, transport)
            config.addAppender(appender)

            val ref = AppenderRef.createAppenderRef("sentry", null, null)

            val loggerConfig = LoggerConfig.createLogger(false, Level.TRACE, "sentry_logger", "true", arrayOf(ref), null, config, null)
            loggerConfig.addAppender(appender, null, null)
            config.addLogger(SentryAppenderTest::class.java.name, loggerConfig)

            loggerContext.updateLoggers(config)

            appender.start()
            loggerContext.start()

            logger = LogManager.getContext().getLogger(SentryAppenderTest::class.java.name)
        }
    }

    private lateinit var fixture: Fixture

    @AfterTest
    fun `stop log4j2`() {
        fixture.loggerContext.stop()
    }

    @BeforeTest
    fun `clear MDC`() {
        ThreadContext.clearAll()
    }

    @Test
    fun `converts message`() {
        fixture = Fixture(minimumEventLevel = Level.DEBUG)
        fixture.logger.debug("testing message conversion {}, {}", 1, 2)

        await.untilAsserted {
            verify(fixture.transport).send(check { it: SentryEvent ->
                assertEquals("testing message conversion 1, 2", it.message.formatted)
                assertEquals("testing message conversion {}, {}", it.message.message)
                assertEquals(listOf("1", "2"), it.message.params)
                assertEquals("io.sentry.log4j2.SentryAppenderTest", it.logger)
            })
        }
    }

    @Test
    fun `event date is in UTC`() {
        fixture = Fixture(minimumEventLevel = Level.DEBUG)
        val utcTime = LocalDateTime.now(ZoneId.of("UTC"))

        fixture.logger.debug("testing event date")

        await.untilAsserted {
            verify(fixture.transport).send(check { it: SentryEvent ->
                val eventTime = Instant.ofEpochMilli(it.timestamp.time)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()

                assertTrue { eventTime.plusSeconds(1).isAfter(utcTime) }
                assertTrue { eventTime.minusSeconds(1).isBefore(utcTime) }
            })
        }
    }

    @Test
    fun `converts trace log level to Sentry level`() {
        fixture = Fixture(minimumEventLevel = Level.TRACE)
        fixture.logger.trace("testing trace level")

        await.untilAsserted {
            verify(fixture.transport).send(check { it: SentryEvent ->
                assertEquals(SentryLevel.DEBUG, it.level)
            })
        }
    }

    @Test
    fun `converts debug log level to Sentry level`() {
        fixture = Fixture(minimumEventLevel = Level.DEBUG)
        fixture.logger.debug("testing debug level")

        await.untilAsserted {
            verify(fixture.transport).send(check { it: SentryEvent ->
                assertEquals(SentryLevel.DEBUG, it.level)
            })
        }
    }

    @Test
    fun `converts info log level to Sentry level`() {
        fixture = Fixture(minimumEventLevel = Level.INFO)
        fixture.logger.info("testing info level")

        await.untilAsserted {
            verify(fixture.transport).send(check { it: SentryEvent ->
                assertEquals(SentryLevel.INFO, it.level)
            })
        }
    }

    @Test
    fun `converts warn log level to Sentry level`() {
        fixture = Fixture(minimumEventLevel = Level.WARN)
        fixture.logger.warn("testing warn level")

        await.untilAsserted {
            verify(fixture.transport).send(check { it: SentryEvent ->
                assertEquals(SentryLevel.WARNING, it.level)
            })
        }
    }

    @Test
    fun `converts error log level to Sentry level`() {
        fixture = Fixture(minimumEventLevel = Level.ERROR)
        fixture.logger.error("testing error level")

        await.untilAsserted {
            verify(fixture.transport).send(check { it: SentryEvent ->
                assertEquals(SentryLevel.ERROR, it.level)
            })
        }
    }

    @Test
    fun `converts fatal log level to Sentry level`() {
        fixture = Fixture(minimumEventLevel = Level.FATAL)
        fixture.logger.fatal("testing fatal level")

        await.untilAsserted {
            verify(fixture.transport).send(check { it: SentryEvent ->
                assertEquals(SentryLevel.FATAL, it.level)
            })
        }
    }

    @Test
    fun `attaches thread information`() {
        fixture = Fixture(minimumEventLevel = Level.WARN)
        fixture.logger.warn("testing thread information")

        await.untilAsserted {
            verify(fixture.transport).send(check { it: SentryEvent ->
                assertNotNull(it.getExtra("thread_name"))
            })
        }
    }

    @Test
    fun `sets tags from ThreadContext`() {
        fixture = Fixture(minimumEventLevel = Level.WARN)
        ThreadContext.put("key", "value")
        fixture.logger.warn("testing MDC tags")

        await.untilAsserted {
            verify(fixture.transport).send(check { it: SentryEvent ->
                assertEquals(mapOf("key" to "value"), it.contexts["MDC"])
            })
        }
    }

    @Test
    fun `does not create MDC context when no MDC tags are set`() {
        fixture = Fixture(minimumEventLevel = Level.WARN)
        fixture.logger.warn("testing without MDC tags")

        await.untilAsserted {
            verify(fixture.transport).send(check { it: SentryEvent ->
                assertFalse(it.contexts.containsKey("MDC"))
            })
        }
    }

    @Test
    fun `attaches throwable`() {
        fixture = Fixture(minimumEventLevel = Level.WARN)
        val throwable = RuntimeException("something went wrong")
        fixture.logger.warn("testing throwable", throwable)

        await.untilAsserted {
            verify(fixture.transport).send(check { it: SentryEvent ->
                assertEquals(throwable, it.throwable)
            })
        }
    }

    @Test
    fun `sets SDK version`() {
        fixture = Fixture(minimumEventLevel = Level.INFO)
        fixture.logger.info("testing sdk version")

        await.untilAsserted {
            verify(fixture.transport).send(check { it: SentryEvent ->
                assertEquals(BuildConfig.SENTRY_LOG4J2_SDK_NAME, it.sdk.name)
                assertEquals(BuildConfig.VERSION_NAME, it.sdk.version)
                assertNotNull(it.sdk.packages)
                assertTrue(it.sdk.packages!!.any { pkg ->
                    "maven:sentry-log4j2" == pkg.name &&
                        BuildConfig.VERSION_NAME == pkg.version
                })
            })
        }
    }

    @Test
    fun `attaches breadcrumbs with level higher than minimumBreadcrumbLevel`() {
        fixture = Fixture(minimumBreadcrumbLevel = Level.DEBUG, minimumEventLevel = Level.WARN)
        val utcTime = LocalDateTime.now(ZoneId.of("UTC"))

        fixture.logger.debug("this should be a breadcrumb #1")
        fixture.logger.info("this should be a breadcrumb #2")
        fixture.logger.warn("testing message with breadcrumbs")

        await.untilAsserted {
            verify(fixture.transport).send(check { it: SentryEvent ->
                assertEquals(2, it.breadcrumbs.size)
                val breadcrumb = it.breadcrumbs[0]
                val breadcrumbTime = Instant.ofEpochMilli(it.timestamp.time)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime()
                assertTrue { breadcrumbTime.plusSeconds(1).isAfter(utcTime) }
                assertTrue { breadcrumbTime.minusSeconds(1).isBefore(utcTime) }
                assertEquals("this should be a breadcrumb #1", breadcrumb.message)
                assertEquals("io.sentry.log4j2.SentryAppenderTest", breadcrumb.category)
                assertEquals(SentryLevel.DEBUG, breadcrumb.level)
            })
        }
    }

    @Test
    fun `does not attach breadcrumbs with level lower than minimumBreadcrumbLevel`() {
        fixture = Fixture(minimumBreadcrumbLevel = Level.INFO, minimumEventLevel = Level.WARN)

        fixture.logger.debug("this should NOT be a breadcrumb")
        fixture.logger.info("this should be a breadcrumb")
        fixture.logger.warn("testing message with breadcrumbs")

        await.untilAsserted {
            verify(fixture.transport).send(check { it: SentryEvent ->
                assertEquals(1, it.breadcrumbs.size)
                assertEquals("this should be a breadcrumb", it.breadcrumbs[0].message)
            })
        }
    }

    @Test
    fun `attaches breadcrumbs for default appender configuration`() {
        fixture = Fixture()

        fixture.logger.debug("this should not be a breadcrumb as the level is lower than the minimum INFO")
        fixture.logger.info("this should be a breadcrumb")
        fixture.logger.warn("this should not be sent as the event but be a breadcrumb")
        fixture.logger.error("this should be sent as the event")

        await.untilAsserted {
            verify(fixture.transport).send(check { it: SentryEvent ->
                assertEquals(2, it.breadcrumbs.size)
                assertEquals("this should be a breadcrumb", it.breadcrumbs[0].message)
                assertEquals("this should not be sent as the event but be a breadcrumb", it.breadcrumbs[1].message)
            })
        }
    }
}
