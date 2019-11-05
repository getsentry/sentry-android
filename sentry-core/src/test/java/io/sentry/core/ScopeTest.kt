package io.sentry.core

import io.sentry.core.protocol.User
import java.util.Date
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame

class ScopeTest {
    @Test
    fun `cloning breadcrumb wont have the same references`() {
        val scope = Scope(1)
        val level = SentryLevel.DEBUG
        scope.level = level

        val user = User()
        user.email = "a@a.com"
        user.id = "123"
        user.ipAddress = "123.x"
        user.username = "userName"
        val others = mapOf(Pair("others", "others"))
        user.others = others

        scope.user = user
        scope.transaction = "transaction"

        val fingerprints = listOf("abc", "def")
        scope.fingerprint = fingerprints

        val breadcrumb = Breadcrumb()
        breadcrumb.message = "message"
        val data = mapOf(Pair("data", "data"))
        breadcrumb.data = data
//        val unknown = mapOf(Pair("unknown", "unknown"))
//        breadcrumb.acceptUnknownProperties(unknown)
        val date = Date()
        breadcrumb.timestamp = date
        breadcrumb.type = "type"
//        val level = SentryLevel.DEBUG
        breadcrumb.level = SentryLevel.DEBUG
        breadcrumb.category = "category"

        scope.addBreadcrumb(breadcrumb)
        scope.setTag("tag", "tag")
        scope.setExtra("extra", "extra")

        val clone = scope.clone()

        assertNotNull(clone)
        assertNotSame(scope, clone)
//        assertNotSame(scope.level, clone.level)
        assertNotSame(scope.user, clone.user)
        assertNotSame(scope.fingerprint, clone.fingerprint)
        assertNotSame(scope.breadcrumbs, clone.breadcrumbs)
        assertNotSame(scope.tags, clone.tags)
        assertNotSame(scope.extras, clone.extras)
    }

    @Test
    fun `cloning breadcrumb will have the same values`() {
        val scope = Scope(1)
        val level = SentryLevel.DEBUG
        scope.level = level

        val user = User()
        user.id = "123"

        scope.user = user
        scope.transaction = "transaction"

        val fingerprints = listOf("abc")
        scope.fingerprint = fingerprints

        val breadcrumb = Breadcrumb()
        breadcrumb.message = "message"

        scope.addBreadcrumb(breadcrumb)
        scope.setTag("tag", "tag")
        scope.setExtra("extra", "extra")

        val clone = scope.clone()

        assertNotNull(clone)

        assertEquals(SentryLevel.DEBUG, clone.level)
        assertEquals("transaction", clone.transaction)

        assertEquals("123", clone.user.id)

        assertEquals("abc", clone.fingerprint.first())

        assertEquals("message", clone.breadcrumbs.first().message)

        assertEquals("tag", clone.tags["tag"])
        assertEquals("extra", clone.extras["extra"])
    }
}
