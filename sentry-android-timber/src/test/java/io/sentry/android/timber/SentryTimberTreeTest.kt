package io.sentry.android.timber

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.check
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import io.sentry.core.Breadcrumb
import io.sentry.core.IHub
import io.sentry.core.SentryLevel
import io.sentry.core.getExc
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import timber.log.Timber

class SentryTimberTreeTest {

    private class Fixture {
        val hub = mock<IHub>()

        fun getSut(
            minEventLevel: SentryLevel = SentryLevel.ERROR,
            minBreadcrumbLevel: SentryLevel = SentryLevel.INFO
        ): SentryTimberTree {
            return SentryTimberTree(hub, minEventLevel, minBreadcrumbLevel)
        }
    }
    private val fixture = Fixture()

    @BeforeTest
    fun beforeTest() {
        Timber.uprootAll()
    }

    @Test
    fun `Tree captures an event if min level is equal`() {
        val sut = fixture.getSut()
        sut.e(Throwable())
        verify(fixture.hub).captureEvent(any())
    }

    @Test
    fun `Tree captures an event if min level is higher`() {
        val sut = fixture.getSut()
        sut.wtf(Throwable())
        verify(fixture.hub).captureEvent(any())
    }

    @Test
    fun `Tree won't capture an event if min level is lower`() {
        val sut = fixture.getSut()
        sut.d(Throwable())
        verify(fixture.hub, never()).captureEvent(any())
    }

    @Test
    fun `Tree captures debug level event`() {
        val sut = fixture.getSut(SentryLevel.DEBUG)
        sut.d(Throwable())
        verify(fixture.hub).captureEvent(check {
            assertEquals(SentryLevel.DEBUG, it.level)
        })
    }

    @Test
    fun `Tree captures info level event`() {
        val sut = fixture.getSut(SentryLevel.DEBUG)
        sut.i(Throwable())
        verify(fixture.hub).captureEvent(check {
            assertEquals(SentryLevel.INFO, it.level)
        })
    }

    @Test
    fun `Tree captures warning level event`() {
        val sut = fixture.getSut(SentryLevel.DEBUG)
        sut.w(Throwable())
        verify(fixture.hub).captureEvent(check {
            assertEquals(SentryLevel.WARNING, it.level)
        })
    }

    @Test
    fun `Tree captures error level event`() {
        val sut = fixture.getSut(SentryLevel.DEBUG)
        sut.e(Throwable())
        verify(fixture.hub).captureEvent(check {
            assertEquals(SentryLevel.ERROR, it.level)
        })
    }

    @Test
    fun `Tree captures fatal level event`() {
        val sut = fixture.getSut(SentryLevel.DEBUG)
        sut.wtf(Throwable())
        verify(fixture.hub).captureEvent(check {
            assertEquals(SentryLevel.FATAL, it.level)
        })
    }

    @Test
    fun `Tree captures unknown as debug level event`() {
        val sut = fixture.getSut(SentryLevel.DEBUG)
        sut.log(15, Throwable())
        verify(fixture.hub).captureEvent(check {
            assertEquals(SentryLevel.DEBUG, it.level)
        })
    }

    @Test
    fun `Tree captures an event with an exception`() {
        val sut = fixture.getSut()
        val throwable = Throwable()
        sut.e(throwable)
        verify(fixture.hub).captureEvent(check {
            assertEquals(throwable, it.getExc())
        })
    }

    @Test
    fun `Tree captures an event without an exception`() {
        val sut = fixture.getSut()
        sut.e("message")
        verify(fixture.hub).captureEvent(check {
            assertNull(it.getExc())
        })
    }

    @Test
    fun `Tree captures an event with origin tag`() {
        val sut = fixture.getSut()
        sut.e("message")
        verify(fixture.hub).captureEvent(check {
            assertEquals("SentryTimberIntegration", it.getTag("origin"))
        })
    }

    @Test
    fun `Tree captures an event with TimberTag tag`() {
        val sut = fixture.getSut()
        Timber.plant(sut)
        // only available thru static class
        Timber.tag("tag")
        Timber.e("message")
        verify(fixture.hub).captureEvent(check {
            assertEquals("tag", it.getTag("TimberTag"))
        })
    }

    @Test
    fun `Tree captures an event without TimberTag tag`() {
        val sut = fixture.getSut()
        Timber.plant(sut)
        Timber.e("message")
        verify(fixture.hub).captureEvent(check {
            assertNull(it.getTag("TimberTag"))
        })
    }

    @Test
    fun `Tree captures an event with given message`() {
        val sut = fixture.getSut()
        sut.e("message")
        verify(fixture.hub).captureEvent(check {
            assertEquals("message", it.message.formatted)
        })
    }

    @Test
    fun `Tree captures an event with stacktrace header`() {
        val sut = fixture.getSut()
        sut.e(Throwable())
        verify(fixture.hub).captureEvent(check {
            println(it.message.formatted)
            assertEquals("java.lang.Throwable", it.message.formatted)
        })
    }

    @Test
    fun `Tree captures an event with message and stacktrace, but only message is available`() {
        val sut = fixture.getSut()
        val throwable = Throwable()
        sut.e(throwable, "message")
        verify(fixture.hub).captureEvent(check {
            assertEquals("message", it.message.formatted)
        })
    }

    @Test
    fun `Tree adds a breadcrumb if min level is equal`() {
        val sut = fixture.getSut(minEventLevel = SentryLevel.INFO)
        sut.i(Throwable(message = "test"))
        verify(fixture.hub).addBreadcrumb(any<Breadcrumb>())
    }

    @Test
    fun `Tree adds a breadcrumb if min level is higher`() {
        val sut = fixture.getSut(minEventLevel = SentryLevel.INFO)
        sut.e(Throwable(message = "test"))
        verify(fixture.hub).addBreadcrumb(any<Breadcrumb>())
    }

    @Test
    fun `Tree won't add a breadcrumb if min level is lower`() {
        val sut = fixture.getSut(minEventLevel = SentryLevel.DEBUG, minBreadcrumbLevel = SentryLevel.ERROR)
        sut.i(Throwable(message = "test"))
        verify(fixture.hub, never()).addBreadcrumb(any<Breadcrumb>())
    }

    @Test
    fun `Tree won't add a breadcrumb if theres no exception`() {
        val sut = fixture.getSut(minEventLevel = SentryLevel.INFO)
        sut.i("test")
        verify(fixture.hub, never()).addBreadcrumb(any<Breadcrumb>())
    }

    @Test
    fun `Tree adds a breadcrumb with given level`() {
        val sut = fixture.getSut(minEventLevel = SentryLevel.INFO)
        sut.e(Throwable(message = "test"))
        verify(fixture.hub).addBreadcrumb(check<Breadcrumb> {
            assertEquals(SentryLevel.ERROR, it.level)
        })
    }

    @Test
    fun `Tree adds a breadcrumb with exception category`() {
        val sut = fixture.getSut(minEventLevel = SentryLevel.INFO)
        sut.e(Throwable(message = "test"))
        verify(fixture.hub).addBreadcrumb(check<Breadcrumb> {
            assertEquals("exception", it.category)
        })
    }

    @Test
    fun `Tree adds a breadcrumb with error type`() {
        val sut = fixture.getSut(minEventLevel = SentryLevel.INFO)
        sut.e(Throwable(message = "test"))
        verify(fixture.hub).addBreadcrumb(check<Breadcrumb> {
            assertEquals("error", it.type)
        })
    }

    @Test
    fun `Tree adds a breadcrumb with exception message`() {
        val sut = fixture.getSut(minEventLevel = SentryLevel.INFO)
        sut.e(Throwable(message = "test"))
        verify(fixture.hub).addBreadcrumb(check<Breadcrumb> {
            assertEquals("test", it.message)
        })
    }
}
