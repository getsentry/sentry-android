package io.sentry.android

import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
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
        val expected = DateUtils.getDateTime(dateIsoFormat)
        sentryEvent.timestamp = expected

        val jsonEvent = "{\"timestamp\":\"$dateIsoFormat\"}"

        val actual = serializer.deserializeEvent(jsonEvent)

        assertEquals(expected, actual.timestamp)
    }

    @Test
    fun `when deserializing unknown properties, it should be added to unknown field`() {
        val sentryEvent = generateEmptySentryEvent()
        sentryEvent.eventId = null
        sentryEvent.timestamp = null
        sentryEvent.unknown = hashMapOf()
        sentryEvent.unknown["string"] = "test"
        sentryEvent.unknown["int"] = 1
        sentryEvent.unknown["boolean"] = true

        val jsonEvent = "{\"string\":\"test\",\"int\":1,\"boolean\":true}"

        val actual = serializer.deserializeEvent(jsonEvent)

        assertEquals(sentryEvent.unknown["string"], (actual.unknown["string"] as JsonPrimitive).asString)
        assertEquals(sentryEvent.unknown["int"], (actual.unknown["int"] as JsonPrimitive).asInt)
        assertEquals(sentryEvent.unknown["boolean"], (actual.unknown["boolean"] as JsonPrimitive).asBoolean)
    }

    @Test
    fun `when deserializing unknown properties with nested objects, it should be added to unknown field`() {
        val sentryEvent = generateEmptySentryEvent()
        sentryEvent.eventId = null
        sentryEvent.timestamp = null
        sentryEvent.unknown = hashMapOf()

        val objects = hashMapOf<String, Any>()
        objects["int"] = 1
        objects["boolean"] = true

        sentryEvent.unknown["object"] = objects

        val jsonEvent = "{\"object\":{\"int\":1,\"boolean\":true}}"

        val actual = serializer.deserializeEvent(jsonEvent)

        val hashMapObject = sentryEvent.unknown["object"] as HashMap<*, *>
        val hashMapActual = actual.unknown["object"] as JsonObject // gson creates it as JsonObject

        assertEquals(hashMapObject["boolean"], hashMapActual.get("boolean").asBoolean)
        assertEquals(hashMapObject["int"], (hashMapActual.get("int")).asInt)
    }

    @Test
    fun `when serializing unknown field, it should become unknown as json format`() {
        val sentryEvent = generateEmptySentryEvent()
        sentryEvent.unknown = hashMapOf()
        sentryEvent.eventId = null
        sentryEvent.timestamp = null

        val objects = hashMapOf<String, Any>()
        objects["int"] = 1
        objects["boolean"] = true

        sentryEvent.unknown["object"] = objects

        val actual = serializer.serialize(sentryEvent)

        val expected = "{\"unknown\":{\"object\":{\"boolean\":true,\"int\":1}}}"

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
