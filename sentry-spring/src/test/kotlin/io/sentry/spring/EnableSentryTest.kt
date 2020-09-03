package io.sentry.spring

import io.sentry.core.IHub
import io.sentry.core.SentryOptions
import kotlin.test.Test
import org.assertj.core.api.Assertions.assertThat
import org.springframework.boot.context.annotation.UserConfigurations
import org.springframework.boot.test.context.runner.ApplicationContextRunner

class EnableSentryTest {
    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(UserConfigurations.of(AppConfig::class.java))

    @Test
    fun `sets properties from environment on SentryOptions`() {
        contextRunner.withPropertyValues(
            "sentry.dsn=http://key@localhost/proj",
            "sentry.send-default-pii=true",
            "sentry.enable-uncaught-exception-handler=true").run {
            assertThat(it).hasSingleBean(SentryOptions::class.java)
            val options = it.getBean(SentryOptions::class.java)
            assertThat(options.dsn).isEqualTo("http://key@localhost/proj")
            assertThat(options.isSendDefaultPii).isTrue()
            assertThat(options.isEnableUncaughtExceptionHandler).isTrue()
        }

        contextRunner.withPropertyValues(
            "sentry.dsn=",
            "sentry.send-default-pii=false",
            "sentry.enable-uncaught-exception-handler=false").run {
            assertThat(it).hasSingleBean(SentryOptions::class.java)
            val options = it.getBean(SentryOptions::class.java)
            assertThat(options.dsn).isEmpty()
            assertThat(options.isSendDefaultPii).isFalse()
            assertThat(options.isEnableUncaughtExceptionHandler).isFalse()
        }
    }

    @Test
    fun `sets client name and SDK version`() {
        contextRunner.withPropertyValues("sentry.dsn=http://key@localhost/proj")
            .run {
                assertThat(it).hasSingleBean(SentryOptions::class.java)
                val options = it.getBean(SentryOptions::class.java)
                assertThat(options.sentryClientName).isEqualTo("sentry.java.spring")
                assertThat(options.sdkVersion).isNotNull
                assertThat(options.sdkVersion!!.name).isEqualTo("sentry.java.spring")
                assertThat(options.sdkVersion!!.version).isEqualTo(BuildConfig.VERSION_NAME)
                assertThat(options.sdkVersion!!.packages).isNotNull
                assertThat(options.sdkVersion!!.packages!!.map { pkg -> pkg.name }).contains("maven:sentry-spring")
            }
    }

    @Test
    fun `creates Sentry Hub`() {
        contextRunner.withPropertyValues("sentry.dsn=http://key@localhost/proj")
            .run {
                assertThat(it).hasSingleBean(IHub::class.java)
            }
    }

    @Test
    fun `creates SentryRequestFilter`() {
        contextRunner.withPropertyValues("sentry.dsn=http://key@localhost/proj")
            .run {
                assertThat(it).hasSingleBean(SentryRequestFilter::class.java)
            }
    }

    @Test
    fun `creates SentryExceptionResolver`() {
        contextRunner.withPropertyValues("sentry.dsn=http://key@localhost/proj")
            .run {
                assertThat(it).hasSingleBean(SentryExceptionResolver::class.java)
            }
    }

    @EnableSentry
    class AppConfig
}
