package io.sentry

import io.sentry.protocol.SentryId
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.test.assertEquals

class MapSentryEventTest {
    @Test
    fun `constructor creates a non empty event id`() {
        val map = hashMapOf<String, Any>()
        assertNotEquals(SentryId.EMPTY_ID, MapSentryEvent(map).eventId)
    }

    @Test
    fun `constructor defines timestamp after now`() {
        val map = hashMapOf<String, Any>()
        assertTrue(Instant.now().plus(1, ChronoUnit.HOURS).isAfter(MapSentryEvent(map).timestamp.toInstant()))
    }

    @Test
    fun `constructor defines timestamp before hour ago`() {
        val map = hashMapOf<String, Any>()
        assertTrue(Instant.now().minus(1, ChronoUnit.HOURS).isBefore(MapSentryEvent(map).timestamp.toInstant()))
    }

    @Test
    fun `timestamp is formatted in ISO 8601 in UTC with Z format`() {
        // Sentry expects this format:
        val expected = "2000-12-31T23:59:58Z"
        val formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd'T'HH:mm:ssX", Locale.ROOT)
        val date = OffsetDateTime.parse(expected, formatter)
        val map = hashMapOf<String, Any>()
        map["timestamp"] = date
        val actual = MapSentryEvent(map)
        assertEquals(expected, actual.timestampIsoFormat)
    }
}
