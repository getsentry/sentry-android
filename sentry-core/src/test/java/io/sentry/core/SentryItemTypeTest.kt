package io.sentry.core

import kotlin.test.Test
import kotlin.test.assertEquals

class SentryItemTypeTest {

    @Test
    fun `Session enum type has a session type string`() {
        assertEquals("session", SentryItemType.Session.itemType)
    }

    @Test
    fun `Event enum type has a event type string`() {
        assertEquals("event", SentryItemType.Event.itemType)
    }
}
