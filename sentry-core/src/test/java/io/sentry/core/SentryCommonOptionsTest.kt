package io.sentry.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SentryCommonOptionsTest {

    @Test
    fun `applies properties to SentryOptions`() {
        val commonOptions = with(SentryCommonOptions()) {
            dsn = "http://key@localhost/proj"
            readTimeoutMillis = 10
            shutdownTimeout = 20L
            flushTimeoutMillis = 30
            isBypassSecurity = true
            isDebug = true
            setDiagnosticLevel(SentryLevel.INFO)
            maxBreadcrumbs = 100
            release = "1.0.3"
            environment = "production"
            sampleRate = 0.2
            inAppExcludes = listOf("org.springframework")
            inAppIncludes = listOf("com.myapp")
            dist = "my-dist"
            isAttachStacktrace = true
            isAttachThreads = true
            serverName = "host-001"
            this
        }

        val options = SentryOptions()
        commonOptions.applyTo(options)

        assertEquals(10, options.readTimeoutMillis)
        assertEquals(20, options.shutdownTimeout)
        assertEquals(30, options.flushTimeoutMillis)
        assertEquals(true, options.isBypassSecurity)
        assertEquals(true, options.isDebug)
        assertEquals(SentryLevel.INFO, options.diagnosticLevel)
        assertEquals(100, options.maxBreadcrumbs)
        assertEquals("1.0.3", options.release)
        assertEquals("production", options.environment)
        assertEquals(0.2, options.sampleRate)
        assertTrue(options.inAppExcludes.contains("org.springframework"))
        assertTrue(options.inAppIncludes.contains("com.myapp"))
        assertEquals("my-dist", options.dist)
        assertEquals(true, options.isAttachThreads)
        assertEquals(true, options.isAttachStacktrace)
        assertEquals("host-001", options.serverName)
    }
}
