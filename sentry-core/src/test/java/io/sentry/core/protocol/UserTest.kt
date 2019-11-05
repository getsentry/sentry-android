package io.sentry.core.protocol

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame

class UserTest {
    @Test
    fun `cloning breadcrumb wont have the same references`() {
        val user = User()
        user.email = "a@a.com"
        user.id = "123"
        user.ipAddress = "123.x"
        user.username = "userName"
        val others = mapOf(Pair("others", "others"))
        user.others = others
        val unknown = mapOf(Pair("unknown", "unknown"))
        user.acceptUnknownProperties(unknown)

        val clone = user.clone()

        assertNotNull(clone)
        assertNotSame(user, clone)

        assertNotSame(user.others, clone.others)

        assertNotSame(user.unknown, clone.unknown)
    }

    @Test
    fun `cloning breadcrumb will have the same values`() {
        val user = User()
        user.email = "a@a.com"
        user.id = "123"
        user.ipAddress = "123.x"
        user.username = "userName"
        val others = mapOf(Pair("others", "others"))
        user.others = others
        val unknown = mapOf(Pair("unknown", "unknown"))
        user.acceptUnknownProperties(unknown)

        val clone = user.clone()

        assertNotNull(clone)
        assertEquals("a@a.com", clone.email)
        assertEquals("123", clone.id)
        assertEquals("123.x", clone.ipAddress)
        assertEquals("userName", clone.username)
        assertEquals("others", clone.others["others"])
        assertEquals("unknown", clone.unknown["unknown"])
    }
}
