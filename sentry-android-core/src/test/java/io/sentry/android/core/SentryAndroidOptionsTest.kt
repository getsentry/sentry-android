package io.sentry.android.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class SentryAndroidOptionsTest {

    @Test
    fun `init should set clientName`() {
        val sentryOptions = SentryAndroidOptions()

        val clientName = "${BuildConfig.SENTRY_CLIENT_NAME}/${BuildConfig.VERSION_NAME}"

        assertEquals(clientName, sentryOptions.sentryClientName)
    }

    @Test
    fun `init should set SdkInfo`() {
        val sentryOptions = SentryAndroidOptions()

        assertNotNull(sentryOptions.sdkInfo)
        val sdkInfo = sentryOptions.sdkInfo!!

        assertEquals("sentry.java.android", sdkInfo.sdkName)

        // versions change on every release, so lets check only if its not null
        assertNotNull(sdkInfo.versionMajor)
        assertNotNull(sdkInfo.versionMinor)
        assertNotNull(sdkInfo.versionPatchlevel)
    }
}
