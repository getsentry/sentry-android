package io.sentry.core

import kotlin.test.Test
import kotlin.test.assertFailsWith

class HttpTransportFactoryTest {

    @Test
    fun `When HttpTransportFactory doesn't have a valid DSN, it throws InvalidDsnException`() {
        assertFailsWith<InvalidDsnException> { HttpTransportFactory.create(SentryOptions()) }
    }

    @Test
    fun `When HttpTransportFactory doesn't have a well formed DSN-URL, it throws IllegalArgumentException`() {
        val options = SentryOptions().apply {
            dsn = "ttps://key@sentry.io/proj"
        }
        assertFailsWith<IllegalArgumentException> { HttpTransportFactory.create(options) }
    }
}
