package io.sentry.android.core

import com.nhaarman.mockitokotlin2.mock
import io.sentry.core.SentryOptions
import kotlin.test.Test

class NdkIntegrationTest {

    @Test
    fun `NdkIntegration won't throw exception`() {
        // hard to test, lets just check that its not throwing anything
        val integration = NdkIntegration()
        integration.register(mock(), SentryOptions())
    }
}
