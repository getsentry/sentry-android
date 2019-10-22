package io.sentry.core

import io.sentry.core.protocol.User
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame

class HubTest {

    @Test
    fun `when cloning Scope it returns the same values`() {
        val scope = Scope()
        scope.extra["test"] = "test"
        val breadcrumb = Breadcrumb()
        breadcrumb.message = "test"
        scope.breadcrumbs.add(breadcrumb)
        scope.level = SentryLevel.DEBUG
        scope.transaction = "test"
        scope.fingerprint.add("test")
        scope.tags["test"] = "test"
        val user = User()
        user.email = "a@a.com"
        scope.user = user

        val clone = scope.clone()
        assertNotNull(clone)
        assertNotSame(scope, clone)
        assertEquals("test", clone.extra["test"])
        assertEquals("test", clone.breadcrumbs[0].message)
        assertEquals("test", scope.transaction)
        assertEquals("test", scope.fingerprint[0])
        assertEquals("test", clone.tags["test"])
        assertEquals("a@a.com", clone.user.email)
    }
}
