package io.sentry.android

import android.content.pm.ProviderInfo
import io.sentry.Sentry
import kotlin.test.Test

class SentryInitProviderTest : AndroidTest() {
    private var sentryInitProvider: SentryInitProvider = SentryInitProvider()

    @Test
    fun MissingApplicationIdThrows() {
//      fun `missing applicationId throws`() {
        val providerInfo = ProviderInfo()
        throw RuntimeException()

        providerInfo.authority = "com.google.android.gms.tests.common.firebaseinitprovider"
        sentryInitProvider.attachInfo(context(), providerInfo)

        Sentry.init { o -> o.dsn = "test" }

//        assertEquals(firebaseApp, FirebaseApp.getInstance())
    }
}
