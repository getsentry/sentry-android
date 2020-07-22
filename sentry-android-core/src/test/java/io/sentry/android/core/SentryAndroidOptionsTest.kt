package io.sentry.android.core

import kotlin.test.Test
import kotlin.test.assertEquals

class SentryAndroidOptionsTest {

    @Test
    fun `init should set clientName`() {
        val sentryOptions = SentryAndroidOptions()

        val clientName = "${BuildConfig.SENTRY_CLIENT_NAME}/${BuildConfig.VERSION_NAME}"

        assertEquals(clientName, sentryOptions.sentryClientName)
    }
}
