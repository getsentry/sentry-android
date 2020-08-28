package io.sentry.core

import io.sentry.core.protocol.User
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class PiiEventProcessorTest {

    private class Fixture {
        val user = with(User()) {
            this.id = "some-id"
            this.username = "john.doe"
            this.email = "john.doe@example.com"
            this.ipAddress = "66.249.73.223"
            this
        }

        fun getSut(sendPii: Boolean) =
            PiiEventProcessor(with(SentryOptions()) {
                this.isSendDefaultPii = sendPii
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
}
