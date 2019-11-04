package io.sentry.core

import com.nhaarman.mockitokotlin2.*
import io.sentry.core.exception.ExceptionMechanismThrowable
import io.sentry.core.protocol.SentryId
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UncaughtExceptionHandlerIntegrationTest {
    @Test
    fun `when UncaughtExceptionHandlerIntegration is initialized, uncaught handler is unchanged`() {
        val handlerMock = mock<UncaughtExceptionHandler>()
        UncaughtExceptionHandlerIntegration(handlerMock)
        verifyZeroInteractions(handlerMock)
    }

    @Test
    fun `when uncaughtException is called, sentry captures exception`() {
        val handlerMock = mock<UncaughtExceptionHandler>()
        val threadMock = mock<Thread>()
        val throwableMock = mock<Throwable>()
        val hubMock = mock<IHub>()
        val options = SentryOptions()
        val sut = UncaughtExceptionHandlerIntegration(handlerMock)
        sut.register(hubMock, options)
        sut.uncaughtException(threadMock, throwableMock)
        verify(hubMock).captureException(any())
    }

    @Test
    fun `when register is called, current handler is not lost`() {
        val handlerMock = mock<UncaughtExceptionHandler>()
        val threadMock = mock<Thread>()
        val throwableMock = mock<Throwable>()
        val defaultHandlerMock = mock<Thread.UncaughtExceptionHandler>()
        whenever(handlerMock.defaultUncaughtExceptionHandler).thenReturn(defaultHandlerMock)
        val hubMock = mock<IHub>()
        val options = SentryOptions()
        val sut = UncaughtExceptionHandlerIntegration(handlerMock)
        sut.register(hubMock, options)
        sut.uncaughtException(threadMock, throwableMock)
        verify(defaultHandlerMock).uncaughtException(threadMock, throwableMock)
    }

    @Test
    fun `when uncaughtException is called, exception captured has handled=false`() {
        val handlerMock = mock<UncaughtExceptionHandler>()
        val threadMock = mock<Thread>()
        val throwableMock = mock<Throwable>()
        val hubMock = mock<IHub>()
        whenever(hubMock.captureException(any())).thenAnswer { invocation ->
            val e = (invocation.arguments[1] as ExceptionMechanismThrowable)
            assertNotNull(e)
            assertNotNull(e.exceptionMechanism)
            assertTrue(e.exceptionMechanism.handled)
            SentryId.EMPTY_ID
        }
        val options = SentryOptions()
        val sut = UncaughtExceptionHandlerIntegration(handlerMock)
        sut.register(hubMock, options)
        sut.uncaughtException(threadMock, throwableMock)
        verify(hubMock).captureException(any())
    }
}
