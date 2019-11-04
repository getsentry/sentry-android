package io.sentry.core

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import io.sentry.core.protocol.User
import io.sentry.core.transport.AsyncConnection
import java.io.PrintWriter
import java.io.StringWriter
import java.util.Queue
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNotSame

class HubTest {

    class Fixture {
        var sentryOptions: SentryOptions = SentryOptions().apply {
            dsn = dsnString
        }
        var connection: AsyncConnection = mock()
        fun getSut() = SentryClient(sentryOptions, connection)
    }

    private val fixture = Fixture()

    @Test
    fun `when cloning Scope it returns the same values`() {
        val scope = Scope(10)
        scope.setExtra("extra", "extra")
        val breadcrumb = Breadcrumb()
        breadcrumb.message = "message"
        scope.addBreadcrumb(breadcrumb)
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
        assertEquals("extra", clone.extras["extra"])
        assertEquals("message", clone.breadcrumbs.first().message)
        assertEquals("transaction", scope.transaction)
        assertEquals("fingerprint", scope.fingerprint[0])
        assertEquals("tags", clone.tags["tags"])
        assertEquals("a@a.com", clone.user.email)
    }

    @Test
    fun `when hub is initialized, integrations are registered`() {
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

    @Test
    fun `when beforeBreadcrumb returns null, crumb is dropped`() {
        val options = SentryOptions()
        options.beforeBreadcrumb = SentryOptions.BeforeBreadcrumbCallback { null }
        options.dsn = "https://key@sentry.io/proj"
        val sut = Hub(options)
        sut.addBreadcrumb(Breadcrumb())
        var breadcrumbs: Queue<Breadcrumb>? = null
        sut.configureScope { breadcrumbs = it.breadcrumbs }
        assertEquals(0, breadcrumbs!!.size)
    }

    @Test
    fun `when beforeBreadcrumb modifies crumb, crumb is stored modified`() {
        val options = SentryOptions()
        val expected = "expected"
        options.beforeBreadcrumb = SentryOptions.BeforeBreadcrumbCallback { it.message = expected; it }
        options.dsn = "https://key@sentry.io/proj"
        val sut = Hub(options)
        val crumb = Breadcrumb()
        crumb.message = "original"
        sut.addBreadcrumb(crumb)
        var breadcrumbs: Queue<Breadcrumb>? = null
        sut.configureScope { breadcrumbs = it.breadcrumbs }
        assertEquals(expected, breadcrumbs!!.first().message)
    }

    @Test
    fun `when beforeBreadcrumb is null, crumb is stored`() {
        val options = SentryOptions()
        options.beforeBreadcrumb = null
        options.dsn = "https://key@sentry.io/proj"
        val sut = Hub(options)
        val expected = Breadcrumb()
        sut.addBreadcrumb(expected)
        var breadcrumbs: Queue<Breadcrumb>? = null
        sut.configureScope { breadcrumbs = it.breadcrumbs }
        assertEquals(expected, breadcrumbs!!.single())
    }

    @Test
    fun `when beforeSend throws an exception, breadcrumb adds an entry to the data field with exception message and stacktrace`() {
        val exception = Exception("test")
        val sw = StringWriter()
        exception.printStackTrace(PrintWriter(sw))
        val stacktrace = sw.toString()

        val options = SentryOptions()
        options.beforeBreadcrumb = SentryOptions.BeforeBreadcrumbCallback { throw exception }
        options.dsn = "https://key@sentry.io/proj"
        val sut = Hub(options)

        val actual = Breadcrumb()
        sut.addBreadcrumb(actual)

        assertEquals("test", actual.data["sentry:message"])
        assertEquals(stacktrace, actual.data["sentry:stacktrace"])
    }
}
