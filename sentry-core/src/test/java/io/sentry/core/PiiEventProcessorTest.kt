package io.sentry.core

import io.sentry.core.protocol.Request
import io.sentry.core.protocol.User
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PiiEventProcessorTest {

    private class Fixture {
        val user = with(User()) {
            id = "some-id"
            username = "john.doe"
            email = "john.doe@example.com"
            ipAddress = "66.249.73.223"
            this
        }

        val request = with(Request()) {
            headers = mutableMapOf(
                "X-FORWARDED-FOR" to "66.249.73.223",
                "Authorization" to "token",
                "Cookies" to "some-cookies",
                "Safe-Header" to "some-value"
            )
            cookies = "some-cookies"
            this
        }

        fun getSut(sendPii: Boolean) =
            PiiEventProcessor(with(SentryOptions()) {
                isSendDefaultPii = sendPii
                this
            })
    }

    private val fixture = Fixture()

    @Test
    fun `when sendDefaultPii is set to false, removes user data from events`() {
        val eventProcessor = fixture.getSut(sendPii = false)
        val event = SentryEvent()
        event.user = fixture.user

        eventProcessor.process(event, null)

        assertNotNull(event.user.id)
        assertNull(event.user.email)
        assertNull(event.user.username)
        assertNull(event.user.ipAddress)
    }

    @Test
    fun `when sendDefaultPii is set to true, does not remove user data from events`() {
        val eventProcessor = fixture.getSut(sendPii = true)
        val event = SentryEvent()
        event.user = fixture.user

        eventProcessor.process(event, null)

        assertNotNull(event.user)
        assertNotNull(event.user.id)
        assertNotNull(event.user.email)
        assertNotNull(event.user.username)
        assertNotNull(event.user.ipAddress)
    }

    @Test
    fun `when sendDefaultPii is set to false, removes user identifiable request headers data from events`() {
        val eventProcessor = fixture.getSut(sendPii = false)
        val event = SentryEvent()
        event.request = fixture.request

        eventProcessor.process(event, null)

        assertNotNull(event.request.headers)
        assertNull(event.request.headers["Authorization"])
        assertNull(event.request.headers["X-FORWARDED-FOR"])
        assertNull(event.request.headers["Cookies"])
        assertNotNull(event.request.headers["Safe-Header"])
    }

    @Test
    fun `when sendDefaultPii is set to true, does not remove user identifiable request headers data from events`() {
        val eventProcessor = fixture.getSut(sendPii = true)
        val event = SentryEvent()
        event.request = fixture.request

        eventProcessor.process(event, null)

        assertNotNull(event.request.headers)
        assertNotNull(event.request.headers["Authorization"])
        assertNotNull(event.request.headers["X-FORWARDED-FOR"])
        assertNotNull(event.request.headers["Cookies"])
        assertNotNull(event.request.headers["Safe-Header"])
    }
}
