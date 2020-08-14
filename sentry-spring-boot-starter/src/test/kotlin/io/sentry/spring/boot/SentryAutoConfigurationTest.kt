package io.sentry.spring.boot

import io.sentry.core.IHub
import io.sentry.core.Sentry
import io.sentry.core.SentryOptions
import kotlin.test.Test
import org.assertj.core.api.Assertions.assertThat
import org.springframework.boot.autoconfigure.AutoConfigurations
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.boot.test.context.runner.WebApplicationContextRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

class SentryAutoConfigurationTest {
    private val contextRunner = ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(SentryAutoConfiguration::class.java, WebMvcAutoConfiguration::class.java))

    private val webContextRunner = WebApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(SentryAutoConfiguration::class.java))

    @Test
    fun `hub is not created when auto-configuration is disabled`() {
        contextRunner.withPropertyValues("sentry.enabled=false", "sentry.dsn=http://key@localhost/proj")
            .run {
                assertThat(it).doesNotHaveBean(IHub::class.java)
            }
    }

    @Test
    fun `hub is created when auto-configuration is enabled`() {
        contextRunner.withPropertyValues("sentry.enabled=true", "sentry.dsn=http://key@localhost/proj")
            .run {
                assertThat(it).hasSingleBean(IHub::class.java)
            }
    }

    @Test
    fun `hub is created when dsn is provided`() {
        contextRunner.withPropertyValues("sentry.dsn=http://key@localhost/proj")
            .run {
                assertThat(it).hasSingleBean(IHub::class.java)
            }
    }

    @Test
    fun `hub is not created when dsn is provided but sentry is disabled`() {
        contextRunner.withPropertyValues("sentry.enabled=false", "sentry.dsn=http://key@localhost/proj")
            .run {
                assertThat(it).doesNotHaveBean(IHub::class.java)
            }
    }

    @Test
    fun `OptionsConfiguration is created if custom one is not provided`() {
        contextRunner.withPropertyValues("sentry.dsn=http://key@localhost/proj")
            .run {
                assertThat(it).hasSingleBean(Sentry.OptionsConfiguration::class.java)
            }
    }

    @Test
    fun `OptionsConfiguration is not created if custom one is provided`() {
        contextRunner.withPropertyValues("sentry.dsn=http://key@localhost/proj")
            .withUserConfiguration(CustomOptionsConfigurationConfiguration::class.java)
            .run {
                assertThat(it).hasSingleBean(Sentry.OptionsConfiguration::class.java)
                assertThat(it.getBean(Sentry.OptionsConfiguration::class.java, "customOptionsConfiguration")).isNotNull
            }
    }

    @Test
    fun `does not register event processors for non web-servlet application type`() {
        contextRunner.withPropertyValues("sentry.dsn=http://key@localhost/proj")
            .run {
                assertThat(it).doesNotHaveBean(SentryRequestHttpServletRequestProcessor::class.java)
                assertThat(it).doesNotHaveBean(SentryUserHttpServletRequestProcessor::class.java)
            }
    }

    @Test
    fun `registers event processors for web servlet application type`() {
        webContextRunner.withPropertyValues("sentry.dsn=http://key@localhost/proj")
            .run {
                assertThat(it).hasSingleBean(SentryRequestHttpServletRequestProcessor::class.java)
                assertThat(it).hasSingleBean(SentryUserHttpServletRequestProcessor::class.java)
            }
    }

    @Configuration(proxyBeanMethods = false)
    open class CustomOptionsConfigurationConfiguration {

        @Bean
        open fun customOptionsConfiguration() = Sentry.OptionsConfiguration<SentryOptions>() {
        }
    }
}
