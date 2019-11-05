package io.sentry.core

import java.util.Date
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class BreadcrumbTest {
    @Test
    fun `cloning breadcrumb wont have the same references`() {
        val breadcrumb = Breadcrumb()
        breadcrumb.message = "message"
        val data = mapOf(Pair("data", "data"))
        breadcrumb.data = data
        val unknown = mapOf(Pair("unknown", "unknown"))
        breadcrumb.acceptUnknownProperties(unknown)
        val date = Date()
        breadcrumb.timestamp = date
        breadcrumb.type = "type"
        val level = SentryLevel.DEBUG
        breadcrumb.level = level
        breadcrumb.category = "category"

        val clone = breadcrumb.clone()

        assertNotNull(clone)
        assertNotSame(breadcrumb, clone)
        assertNotSame(breadcrumb.timestamp, clone.timestamp)

        assertNotSame(breadcrumb.data, clone.data)

        assertNotSame(breadcrumb.unknown, clone.unknown)

//        assertNotSame(breadcrumb.level, clone.level) why still the same?
        assertSame(breadcrumb.level, clone.level)

        breadcrumb.level = SentryLevel.FATAL
        assertNotSame(breadcrumb.level, clone.level)
    }

    @Test
    fun `cloning breadcrumb will have the same values`() {
        val breadcrumb = Breadcrumb()
        breadcrumb.message = "message"
        val data = mapOf(Pair("data", "data"))
        breadcrumb.data = data
        val unknown = mapOf(Pair("unknown", "unknown"))
        breadcrumb.acceptUnknownProperties(unknown)
        val date = Date()
        val dateIso = DateUtils.getTimestamp(date)
        breadcrumb.timestamp = date
        breadcrumb.type = "type"
        val level = SentryLevel.DEBUG
        breadcrumb.level = level
        breadcrumb.category = "category"

        val clone = breadcrumb.clone()

        assertEquals("message", clone.message)
        assertEquals("data", clone.data["data"])
        assertEquals("unknown", clone.unknown["unknown"])
        assertEquals("type", clone.type)
        assertEquals(SentryLevel.DEBUG, clone.level)
        assertEquals("category", clone.category)
        assertEquals(dateIso, DateUtils.getTimestamp(clone.timestamp))
    }
}
