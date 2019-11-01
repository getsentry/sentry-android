package io.sentry.android.core

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.nhaarman.mockitokotlin2.*
import io.sentry.core.*
import org.junit.runner.RunWith
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

@RunWith(AndroidJUnit4::class)
class EnvelopeFileObserverTest {

    private class Fixture {
        var path: String? = "."
        var envelopeSender: IEnvelopeSender = mock()
        var logger: ILogger? = mock()

        init {
            val options = SentryOptions()
            options.isDebug = true
            options.setLogger(logger)
        }

        fun getSut(): EnvelopeFileObserver {
            return EnvelopeFileObserver(path, envelopeSender, logger)
        }
    }

    private val fixture = Fixture()

    @Test
    fun `envelope sender is called with fully qualified path`() {
        val sut = fixture.getSut()
        val param = "file-name.txt"
        sut.onEvent(0, param)
        verify(fixture.envelopeSender).processEnvelopeFile(fixture.path + "/" + param)
    }

    @Test
    fun `when event is fired with null path, envelope reader is not called`() {
        val sut = fixture.getSut()
        sut.onEvent(0, null)
        verify(fixture.envelopeSender, never()).processEnvelopeFile(anyOrNull())
    }

    @Test
    fun `when null is passed as a path, ctor throws`() {
        fixture.path = null;
        val exception = assertFailsWith<Exception> { fixture.getSut() }
        assertEquals("File path is required.", exception.message)
    }
}
