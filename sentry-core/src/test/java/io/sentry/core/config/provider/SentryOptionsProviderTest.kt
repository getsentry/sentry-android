package io.sentry.core.config.provider

import kotlin.test.Test

class SentryOptionsProviderTest {

    @Test
    fun `loads properties from external source`() {
        val options = SentryOptionsProvider.create(true).resolve()
        println(options.dsn)
    }
}
