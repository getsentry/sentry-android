package io.sentry.core

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import io.sentry.core.protocol.User
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame

class HubTest {

    @Test
    fun `when cloning Scope it returns the same values`() {
        val scope = Scope(10)
        scope.extra["extra"] = "extra"
        val breadcrumb = Breadcrumb()
        breadcrumb.message = "message"
        scope.breadcrumbs.add(breadcrumb)
        scope.level = SentryLevel.DEBUG
        scope.transaction = "transaction"
        scope.fingerprint.add("fingerprint")
        scope.tags["tags"] = "tags"
        val user = User()
        user.email = "a@a.com"
        scope.user = user

        val clone = scope.clone()
        assertNotNull(clone)
        assertNotSame(scope, clone)
        assertEquals("extra", clone.extra["extra"])
        assertEquals("message", clone.breadcrumbs.first().message)
        assertEquals("transaction", scope.transaction)
        assertEquals("fingerprint", scope.fingerprint[0])
        assertEquals("tags", clone.tags["tags"])
        assertEquals("a@a.com", clone.user.email)
    }

    @Test
    fun `when hub is initialized, integrations are registed`() {
        val integrationMock = mock<Integration>()
        val options = SentryOptions()
        options.dsn = "https://key@sentry.io/proj"
        options.addIntegration(integrationMock)
        val expected = Hub(options)
        verify(integrationMock).register(expected, options)
    }

    @Test
    fun `when hub is initialized, breadcrumbs are capped as per options`() {
        val options = SentryOptions()
        options.maxBreadcrumbs = 5
        options.dsn = "https://key@sentry.io/proj"
        val sut = Hub(options)
        (1..10).forEach { _ -> sut.addBreadcrumb(Breadcrumb()) }
        var actual = 0
        sut.configureScope {
            actual = it.breadcrumbs.size
        }
        assertEquals(options.maxBreadcrumbs, actual)
    }
}
