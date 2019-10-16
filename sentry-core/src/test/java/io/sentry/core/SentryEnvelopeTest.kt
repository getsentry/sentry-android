package io.sentry.core

import io.sentry.core.protocol.SentryId
import kotlin.test.Test
import kotlin.test.assertTrue
import com.google.gson.Gson

abstract class SentryEnvelopeTest {

    @Test
    fun `test`() {
        val sut = createSampleEnvelope()
        val gson = Gson()
        gson.toJson()
    }

    private fun createSampleEnvelope() : SentryEnvelope {
        val size = 1024L
        val header = SentryEnvelopeItemHeader("event", size, "application/json", "event.json")
        val event = SentryEnvelopeItem(header, ByteArray(size.toInt()).toTypedArray())

        val sentryId = SentryId()
        val auth: String? = null
        return SentryEnvelope(sentryId, auth, listOf(event))
    }
}
