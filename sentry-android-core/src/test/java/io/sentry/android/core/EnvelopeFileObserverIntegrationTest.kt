package io.sentry.android.core

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.sentry.core.SentryOptions
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class EnvelopeFileObserverIntegrationTest {
    @Test
    fun `when instance from getOutboxFileObserver, options getOutboxPath is used`() {
        var options = SentryOptions()
        options.cacheDirPath = "some_dir"

        val sut = EnvelopeFileObserverIntegration.getCachedEnvelopeFileObserver()
        assertEquals(options.outboxPath, sut.getPath(options))
    }

    @Test
    fun `when instance from getCachedEnvelopeFileObserver, options getCacheDirPath + cache dir is used`() {
        var options = SentryOptions()
        options.cacheDirPath = "some_dir"

        val sut = EnvelopeFileObserverIntegration.getCachedEnvelopeFileObserver()
        assertEquals(options.cacheDirPath + "/cached", sut.getPath(options))
    }
}
