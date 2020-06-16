package io.sentry.android.core

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import io.sentry.core.ILogger
import io.sentry.core.SentryLevel
import io.sentry.core.SentryOptions
import kotlin.test.Test
import kotlin.test.assertFalse

class NdkIntegrationTest {

    @Test
    fun `NdkIntegration won't throw exception`() {
        // hard to test, lets just check that its not throwing anything
        val integration = NdkIntegration()
        val logger = mock<ILogger>()
        val options = SentryOptions().apply {
            setLogger(logger)
            isDebug = true
        }
        // it'll throw ClassNotFoundException because this class is not available on the test runtime.
        integration.register(mock(), options)
        verify(logger, never()).log(eq(SentryLevel.ERROR), eq("Failed to load (UnsatisfiedLinkError) SentryNdk."), any())
        verify(logger, never()).log(eq(SentryLevel.ERROR), eq("Failed to initialize SentryNdk."), any())
    }

    @Test
    fun `NdkIntegration won't init if ndk integration is disabled`() {
        val integration = NdkIntegration()
        val logger = mock<ILogger>()
        val options = SentryOptions().apply {
            setLogger(logger)
            isDebug = true
            isEnableNdk = false
        }
        // it'll throw ClassNotFoundException because this class is not available on the test runtime.
        integration.register(mock(), options)
        verify(logger, never()).log(any(), any<String>(), any())
        verify(logger, never()).log(any(), any())
        assertFalse(options.isEnableNdk)
    }
}
