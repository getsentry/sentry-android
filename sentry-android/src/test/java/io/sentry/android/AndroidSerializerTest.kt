package io.sentry.android

import io.sentry.DateUtils
import io.sentry.SentryEvent
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class AndroidSerializerTest {

    private val serializer = AndroidSerializer()

    @Test
    fun `when serializing SentryEvent-SentryId object, it should become a event_id json without dashes`() {
        val sentryEvent = generateEmptySentryEvent()
        sentryEvent.timestamp = null

        val actual = serializer.serialize(sentryEvent)

        val expected = "{\"event_id\":\"${sentryEvent.eventId}\"}"

        assertEquals(expected, actual)
    }

    @Test
    fun `when deserializing event_id, it should become a SentryEvent-SentryId uuid`() {
        val expected = UUID.randomUUID().toString().replace("-", "")
        val jsonEvent = "{\"event_id\":\"$expected\"}"

        val actual = serializer.deserializeEvent(jsonEvent)

        assertEquals(expected, actual.eventId.toString())
    }

    @Test
    fun `when serializing SentryEvent-Date, it should become a timestamp json ISO format`() {
        val sentryEvent = generateEmptySentryEvent()
        val dateIsoFormat = "2000-12-31T23:59:58Z"
        sentryEvent.eventId = null
        sentryEvent.timestamp = DateUtils.getDateTime(dateIsoFormat)

        val expected = "{\"timestamp\":\"$dateIsoFormat\"}"

        val actual = serializer.serialize(sentryEvent)

        assertEquals(expected, actual)
    }

    @Test
    fun `when deserializing timestamp, it should become a SentryEvent-Date`() {
        val sentryEvent = generateEmptySentryEvent()
        val dateIsoFormat = "2000-12-31T23:59:58Z"
        sentryEvent.eventId = null
        sentryEvent.timestamp = DateUtils.getDateTime(dateIsoFormat)

        val expected = "{\"timestamp\":\"$dateIsoFormat\"}"

        val actual = serializer.serialize(sentryEvent)

        assertEquals(expected, actual)
    }

    private fun generateEmptySentryEvent(): SentryEvent {
        return SentryEvent().apply {
            setBreadcrumbs(null)
            setTags(null)
            setExtra(null)
            fingerprint = null
            contexts = null
        }
    }
}
