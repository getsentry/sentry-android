package io.sentry.core

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.verifyNoMoreInteractions
import com.nhaarman.mockitokotlin2.whenever
import kotlin.test.Test

class SendCachedEventFireAndForgetIntegrationTest {
    private class Fixture {
        var hub: HubWrapper? = mock()
        var logger: ILogger? = mock()
        var options = SentryOptions()
        var callback = mock<SendCachedEventFireAndForgetIntegration.SendFireAndForgetFactory>()

        init {
            options.isDebug = true
            options.setLogger(logger)
        }

        fun getSut(): SendCachedEventFireAndForgetIntegration {
            return SendCachedEventFireAndForgetIntegration(callback)
        }
    }

    private val fixture = Fixture()

    @Test
    fun `when cacheDirPath returns null, register logs and exists`() {
        fixture.options.cacheDirPath = null
        val sut = fixture.getSut()
        whenever(fixture.hub!!.isIntegrationAvailable(any())).thenReturn(true)
        sut.register(fixture.hub!!, fixture.options)
        verify(fixture.hub, times(1))!!.isIntegrationAvailable(any())
        verify(fixture.logger)!!.log(eq(SentryLevel.WARNING), eq("No cache dir path is defined in options."))
        verifyNoMoreInteractions(fixture.hub)
    }
}
