package io.sentry.test

import com.nhaarman.mockitokotlin2.mock
import io.sentry.core.GsonSerializer
import io.sentry.core.SentryEnvelope
import io.sentry.core.SentryEvent
import io.sentry.core.SentryOptions

private val options = SentryOptions().apply {
    setSerializer(GsonSerializer(mock(), envelopeReader))
}

/**
 * Verifies is [SentryEnvelope] matches condition specified by [fn].
 */
fun assertEventMatches(envelope: SentryEnvelope, fn: (event: SentryEvent) -> Unit) {
    val event = envelope.items.first().getEvent(options.serializer)!!
    fn(event)
}
