package io.sentry.core.transport

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import io.sentry.core.ISerializer
import io.sentry.core.SentryEvent
import io.sentry.core.SentryOptions
import java.io.IOException
import java.net.HttpURLConnection
import java.net.Proxy
import java.net.URI
import java.net.URL
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class HttpTransportTest {

    private class Fixture {
        val dsn: URL = URI.create("http://key@localhost/proj").toURL()
        val serializer = mock<ISerializer>()
        var proxy: Proxy? = null
        var requestUpdater = IConnectionConfigurator {}
        var connectionTimeout = 1000
        var readTimeout = 500
        var bypassSecurity = false
        val connection = mock<HttpURLConnection>()

        init {
            whenever(connection.outputStream).thenReturn(mock())
            whenever(connection.inputStream).thenReturn(mock())
        }

        fun getSUT(): HttpTransport {
            val options = SentryOptions()
            options.setSerializer(serializer)
            options.proxy = proxy

            return object : HttpTransport(options, requestUpdater, connectionTimeout, readTimeout, bypassSecurity, dsn) {
                override fun open(proxy: Proxy?): HttpURLConnection {
                    return connection
                }
            }
        }
    }

    private val fixture = Fixture()

    @Test
    fun `test serializes event`() {
        val transport = fixture.getSUT()

        val event = SentryEvent()

        val result = transport.send(event)

        verify(fixture.serializer).serialize(eq(event), any())
        assertTrue(result.isSuccess)
    }

    @Test
    fun `uses Retry-After header`() {
        val transport = fixture.getSUT()

        whenever(fixture.connection.inputStream).thenThrow(IOException())
        whenever(fixture.connection.getHeaderField(eq("Retry-After"))).thenReturn("4.5")

        val event = SentryEvent()

        val result = transport.send(event)

        verify(fixture.serializer).serialize(eq(event), any())
        assertFalse(result.isSuccess)
//        assertEquals(4500, result.retryMillis)
    }

    @Test
    fun `passes on the response code on error`() {
        val transport = fixture.getSUT()

        whenever(fixture.connection.inputStream).thenThrow(IOException())
        whenever(fixture.connection.responseCode).thenReturn(1234)

        val event = SentryEvent()

        val result = transport.send(event)

        verify(fixture.serializer).serialize(eq(event), any())
        assertFalse(result.isSuccess)
        assertEquals(1234, result.responseCode)
    }

    @Test
    fun `uses the default retry interval if there is no Retry-After header`() {
        val transport = fixture.getSUT()

        whenever(fixture.connection.inputStream).thenThrow(IOException())

        val event = SentryEvent()

        val result = transport.send(event)

        verify(fixture.serializer).serialize(eq(event), any())
        assertFalse(result.isSuccess)
//        assertEquals(RetryingThreadPoolExecutor.HTTP_RETRY_AFTER_DEFAULT_DELAY_MS, result.retryMillis)
    }

    @Test
    fun `failure to get response code doesn't break sending`() {
        val transport = fixture.getSUT()

        whenever(fixture.connection.inputStream).thenThrow(IOException())
        whenever(fixture.connection.responseCode).thenThrow(IOException())

        val event = SentryEvent()

        val result = transport.send(event)

        verify(fixture.serializer).serialize(eq(event), any())
        assertFalse(result.isSuccess)
//        assertEquals(RetryingThreadPoolExecutor.HTTP_RETRY_AFTER_DEFAULT_DELAY_MS, result.retryMillis)
        assertEquals(-1, result.responseCode)
    }

//    @Test
//    fun `reads 429 headers and returns accordingly if there are spaces in between`() {
//        val transport = fixture.getSUT()
//        val retryAfterLimits = transport.getRetryAfterLimits(
//            "50:transaction:key, 2700:default;event;security:organization",
//            "60"
//        )
//        assertEquals(50000L, retryAfterLimits["transaction"])
//        assertEquals(2700000L, retryAfterLimits["default"])
//        assertEquals(2700000L, retryAfterLimits["event"])
//        assertEquals(2700000L, retryAfterLimits["security"])
//    }
//
//    @Test
//    fun `reads 429 headers and returns accordingly if there are no spaces`() {
//        val transport = fixture.getSUT()
//        val retryAfterLimits = transport.getRetryAfterLimits(
//            "50:transaction:key,2700:default;event;security:organization",
//            "60"
//        )
//        assertEquals(50000L, retryAfterLimits["transaction"])
//        assertEquals(2700000L, retryAfterLimits["default"])
//        assertEquals(2700000L, retryAfterLimits["event"])
//        assertEquals(2700000L, retryAfterLimits["security"])
//    }
//
//    @Test
//    fun `reads 429 headers and returns accordingly, if there are a wrong interval, use retry after one`() {
//        val transport = fixture.getSUT()
//        val retryAfterLimits = transport.getRetryAfterLimits(
//            "x:transaction:key,y:default;event;security:organization",
//            "60"
//        )
//        assertEquals(60000L, retryAfterLimits["transaction"])
//        assertEquals(60000L, retryAfterLimits["default"])
//        assertEquals(60000L, retryAfterLimits["event"])
//        assertEquals(60000L, retryAfterLimits["security"])
//    }
//
//    @Test
//    fun `reads 429 headers and returns retry after header if none is set`() {
//        val transport = fixture.getSUT()
//        val retryAfterLimits = transport.getRetryAfterLimits(
//            null,
//            "60"
//        )
//        assertEquals(60000L, retryAfterLimits["default"])
//    }
//
//    @Test
//    fun `reads 429 headers and returns default value if none is set`() {
//        val transport = fixture.getSUT()
//        val retryAfterLimits = transport.getRetryAfterLimits(
//            null,
//            null
//        )
//        assertEquals(60000L, retryAfterLimits["default"])
//    }
}
