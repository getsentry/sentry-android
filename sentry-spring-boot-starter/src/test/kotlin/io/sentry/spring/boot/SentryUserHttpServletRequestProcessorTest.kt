package io.sentry.spring.boot

import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import io.sentry.core.SentryEvent
import kotlin.test.Test
import kotlin.test.assertEquals
import org.springframework.mock.web.MockHttpServletRequest
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.security.Principal

class SentryUserHttpServletRequestProcessorTest {

    private val eventProcessor = SentryUserHttpServletRequestProcessor()

    @Test
    fun `attaches user's IP address to Sentry Event`() {
        val request = MockHttpServletRequest()
        request.remoteAddr = "192.168.0.1"

        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))
        val event = SentryEvent()

        eventProcessor.process(event, null)

        assertEquals("192.168.0.1", event.user.ipAddress)
    }

    @Test
    fun `attaches username to Sentry Event`() {
        val principal = mock<Principal>()
        whenever(principal.name).thenReturn("janesmith")
        val request = MockHttpServletRequest()
        request.userPrincipal = principal

        RequestContextHolder.setRequestAttributes(ServletRequestAttributes(request))
        val event = SentryEvent()

        eventProcessor.process(event, null)

        assertEquals("janesmith", event.user.username)
    }
}
