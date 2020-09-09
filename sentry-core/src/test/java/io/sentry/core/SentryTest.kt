package io.sentry.core

import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import java.io.File
import java.nio.file.Files
import java.util.concurrent.CompletableFuture
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
class SentryTest {

    @BeforeTest
    fun beforeTest() {
        Sentry.close()
    }

    @Test
    fun `outboxDir should be created at initialization`() {
        var sentryOptions: SentryOptions? = null
        Sentry.init {
            it.dsn = "http://key@localhost/proj"
            it.cacheDirPath = getTempPath()
            sentryOptions = it
        }

        val file = File(sentryOptions!!.outboxPath!!)
        assertTrue(file.exists())
        file.deleteOnExit()
    }

    @Test
    fun `envelopesDir should be created at initialization`() {
        var sentryOptions: SentryOptions? = null
        Sentry.init {
            it.dsn = "http://key@localhost/proj"
            it.cacheDirPath = getTempPath()
            sentryOptions = it
        }

        val file = File(sentryOptions!!.cacheDirPath!!)
        assertTrue(file.exists())
        file.deleteOnExit()
    }

    @Test
    fun `sessionDir should be created at initialization`() {
        var sentryOptions: SentryOptions? = null
        Sentry.init {
            it.dsn = "http://key@localhost/proj"
            it.cacheDirPath = getTempPath()
            sentryOptions = it
        }

        val file = File(sentryOptions!!.sessionsPath!!)
        assertTrue(file.exists())
        file.deleteOnExit()
    }

    @Test
    fun `Init sets SystemOutLogger if logger is NoOp and debug is enabled`() {
        var sentryOptions: SentryOptions? = null
        Sentry.init {
            it.dsn = "http://key@localhost/proj"
            it.cacheDirPath = getTempPath()
            sentryOptions = it
            it.isDebug = true
        }

        assertTrue((sentryOptions!!.logger as DiagnosticLogger).logger is SystemOutLogger)
    }

    @Test
    fun `Init sets GsonSerializer if serializer is NoOp`() {
        var sentryOptions: SentryOptions? = null
        Sentry.init {
            it.dsn = "http://key@localhost/proj"
            it.cacheDirPath = getTempPath()
            sentryOptions = it
        }

        assertTrue(sentryOptions!!.serializer is GsonSerializer)
    }

    @Test
    fun `scope changes are isolated to a thread`() {
        Sentry.init {
            it.dsn = "http://key@localhost/proj"
        }
        Sentry.configureScope {
            it.setTag("a", "a")
        }

        CompletableFuture.runAsync {
            Sentry.configureScope {
                it.setTag("b", "b")
            }

            Sentry.configureScope {
                assertEquals(setOf("a", "b"), it.tags.keys)
            }
        }.get()

        Sentry.configureScope {
            assertEquals(setOf("a"), it.tags.keys)
        }
    }

    @Test
    fun `warns about multiple Sentry initializations`() {
        val logger = mock<ILogger>()
        Sentry.init {
            it.dsn = "http://key@localhost/proj"
        }
        Sentry.init {
            it.dsn = "http://key@localhost/proj"
            it.isDebug = true
            it.setLogger(logger)
        }
        verify(logger).log(eq(SentryLevel.WARNING), eq("Sentry has been already initialized. Previous configuration will be overwritten."))
    }

    private fun getTempPath(): String {
        val tempFile = Files.createTempDirectory("cache").toFile()
        tempFile.delete()

        // sanity check
        assertFalse(tempFile.exists())
        return tempFile.absolutePath
    }
}
