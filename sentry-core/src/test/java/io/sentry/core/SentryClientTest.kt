package io.sentry.core

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import io.sentry.core.protocol.User
import io.sentry.core.transport.AsyncConnection
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.mockito.Mockito

class SentryClientTest {

    class Fixture {
        var sentryOptions: SentryOptions = SentryOptions().apply {
            dsn = dsnString
        }
        var connection: AsyncConnection = mock()
        fun getSut() = SentryClient(sentryOptions, connection)
    }

    private val fixture = Fixture()

    @Test
    fun `when fixture is unchanged, client is enabled`() {
        val sut = fixture.getSut()
        assertTrue(sut.isEnabled)
    }

    @Test
    @Ignore("Not implemented")
    fun `when dsn is an invalid string, client is disabled`() {
        fixture.sentryOptions.dsn = "invalid-dsn"
        val sut = fixture.getSut()
        assertFalse(sut.isEnabled)
    }

    @Test
    @Ignore("Not implemented")
    fun `when dsn is null, client is disabled`() {
        fixture.sentryOptions.dsn = null
        val sut = fixture.getSut()
        assertFalse(sut.isEnabled)
    }

    @Test
    fun `when dsn without private key is valid, client is enabled`() {
        fixture.sentryOptions.dsn = dsnString
        val sut = fixture.getSut()
        assertTrue(sut.isEnabled)
    }

    @Test
    fun `when dsn with secret is valid, client is enabled`() {
        fixture.sentryOptions.dsn = dsnStringLegacy
        val sut = fixture.getSut()
        assertTrue(sut.isEnabled)
    }

    @Test
    fun `when client is closed, client gets disabled`() {
        val sut = fixture.getSut()
        assertTrue(sut.isEnabled)
        sut.close()
        assertFalse(sut.isEnabled)
    }

    @Test
    fun `when beforeSend is set, callback is invoked`() {
        var invoked = false
        fixture.sentryOptions.setBeforeSend { e ->
            invoked = true
            e
        }
        val sut = fixture.getSut()
        sut.captureEvent(SentryEvent())
        assertTrue(invoked)
    }

    @Test
    fun `when beforeSend is returns null, event is dropped`() {
        fixture.sentryOptions.setBeforeSend { null }
        val sut = fixture.getSut()
        val event = SentryEvent()
        sut.captureEvent(event)
        verify(fixture.connection, never()).send(event)
    }

    @Test
    fun `when beforeSend is returns new instance, new instance is sent`() {
        val expected = SentryEvent()
        fixture.sentryOptions.setBeforeSend { expected }
        val sut = fixture.getSut()
        val actual = SentryEvent()
        sut.captureEvent(actual)
        verify(fixture.connection, never()).send(actual)
        verify(fixture.connection, times(1)).send(expected)
    }

    @Test
    fun `when captureMessage is called, sentry event contains formatted message`() {
        var sentEvent: SentryEvent? = null
        fixture.sentryOptions.setBeforeSend { e ->
            sentEvent = e
            e
        }
        val sut = fixture.getSut()
        val actual = "actual message"
        sut.captureMessage(actual)
        assertEquals(actual, sentEvent!!.message.formatted)
    }

    @Test
    fun `when event has release, value from options not applied`() {
        val event = SentryEvent()
        val expected = "original"
        fixture.sentryOptions.environment = "not to be applied"
        event.release = expected
        val sut = fixture.getSut()
        sut.captureEvent(event)
        verify(fixture.connection).send(event)
        assertEquals(expected, event.release)
    }

    @Test
    fun `when event doesn't have release, value from options applied`() {
        val event = SentryEvent()
        val expected = "original"
        fixture.sentryOptions.release = expected
        val sut = fixture.getSut()
        sut.captureEvent(event)
        verify(fixture.connection).send(event)
        assertEquals(expected, event.release)
    }

    @Test
    fun `when event has environment, value from options not applied`() {
        val event = SentryEvent()
        val expected = "original"
        fixture.sentryOptions.environment = "not to be applied"
        event.environment = expected
        val sut = fixture.getSut()
        sut.captureEvent(event)
        verify(fixture.connection).send(event)
        assertEquals(expected, event.environment)
    }

    @Test
    fun `when event doesn't have environment, value from options applied`() {
        val event = SentryEvent()
        val expected = "original"
        fixture.sentryOptions.environment = expected
        val sut = fixture.getSut()
        sut.captureEvent(event)
        assertEquals(expected, event.environment)
    }

    @Test
    fun `when captureEvent with scope, event should have its data if not set`() {
        val event = SentryEvent()
        val scope = createScope()

        val sut = fixture.getSut()

        sut.captureEvent(event, scope)
        assertEquals("message", event.breadcrumbs[0].message)
        assertEquals("extra", event.extras["extra"])
        assertEquals("tags", event.tags["tags"])
        assertEquals("fp", event.fingerprints[0])
        assertEquals("transaction", event.transaction)
        assertEquals("id", event.user.id)
        assertEquals(SentryLevel.FATAL, event.level)
    }

    @Test
    fun `when captureEvent with scope, event data has priority over scope but level and it should append extras, tags and breadcrumbs`() {
        val event = createEvent()

        val scope = createScope()

        val sut = fixture.getSut()

        sut.captureEvent(event, scope)

        // breadcrumbs are appending
        assertEquals("eventMessage", event.breadcrumbs[0].message)
        assertEquals("message", event.breadcrumbs[1].message)

        // extras are appending
        assertEquals("eventExtra", event.extras["eventExtra"])
        assertEquals("extra", event.extras["extra"])

        // tags are appending
        assertEquals("eventTag", event.tags["eventTag"])
        assertEquals("tags", event.tags["tags"])

        // fingerprint is replaced
        assertEquals("eventFp", event.fingerprints[0])
        assertEquals(1, event.fingerprints.size)

        assertEquals("eventTransaction", event.transaction)

        assertEquals("eventId", event.user.id)

        assertEquals(SentryLevel.FATAL, event.level)
    }

    @Test
    fun `when captureEvent with scope, event extras and tags are only append if key is absent`() {
        val event = createEvent()

        val scope = createScope()
        scope.setExtra("eventExtra", "extra")
        scope.setTag("eventTag", "tags")

        val sut = fixture.getSut()

        sut.captureEvent(event, scope)

        // extras are appending
        assertEquals("eventExtra", event.extras["eventExtra"])

        // tags are appending
        assertEquals("eventTag", event.tags["eventTag"])
    }

    @Test
    fun `when captureEvent with scope, event should have its level if set`() {
        val event = SentryEvent()
        event.level = SentryLevel.DEBUG
        val scope = createScope()

        val sut = fixture.getSut()

        sut.captureEvent(event, scope)
        assertEquals(SentryLevel.FATAL, event.level)
    }

    @Test
    fun `when captureEvent with sampling, some events not captured`() {
        fixture.sentryOptions.sampling = 0.000000001
        val sut = fixture.getSut()

        val allEvents = 10
        (0..allEvents).forEach { _ -> sut.captureEvent(SentryEvent()) }
        assertTrue(allEvents > Mockito.mockingDetails(fixture.connection).invocations.size)
    }

    @Test
    fun `when captureEvent without sampling, all events are captured`() {
        fixture.sentryOptions.sampling = null
        val sut = fixture.getSut()

        val allEvents = 10
        (0..allEvents).forEach { _ -> sut.captureEvent(SentryEvent()) }
        assertEquals(allEvents, Mockito.mockingDetails(fixture.connection).invocations.size - 1) // 1 extra invocation outside .send()
    }

    private fun createScope(): Scope {
        return Scope(fixture.sentryOptions.maxBreadcrumbs).apply {
            addBreadcrumb(Breadcrumb().apply {
                message = "message"
            })
            setExtra("extra", "extra")
            setTag("tags", "tags")
            fingerprint.add("fp")
            transaction = "transaction"
            level = SentryLevel.FATAL
            user = User().apply {
                id = "id"
            }
        }
    }

    private fun createEvent(): SentryEvent {
        return SentryEvent().apply {
            addBreadcrumb(Breadcrumb().apply {
                message = "eventMessage"
            })
            setExtra("eventExtra", "eventExtra")
            setTag("eventTag", "eventTag")
            fingerprints = listOf("eventFp")
            transaction = "eventTransaction"
            level = SentryLevel.DEBUG
            user = User().apply {
                id = "eventId"
            }
        }
    }
}
