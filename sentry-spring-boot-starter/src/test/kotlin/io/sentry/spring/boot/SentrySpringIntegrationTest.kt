package io.sentry.spring.boot

import com.nhaarman.mockitokotlin2.check
import com.nhaarman.mockitokotlin2.verify
import io.sentry.core.IHub
import io.sentry.core.Sentry
import io.sentry.core.SentryEvent
import io.sentry.core.transport.ITransport
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.junit.Test
import org.junit.runner.RunWith
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.crypto.factory.PasswordEncoderFactories
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.provisioning.InMemoryUserDetailsManager
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter
import org.springframework.test.context.junit4.SpringRunner
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RunWith(SpringRunner::class)
@SpringBootTest(
    classes = [App::class],
    webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
    properties = ["sentry.dsn=http://key@localhost/proj"]
)
class SentrySpringIntegrationTest {

    @MockBean
    lateinit var transport: ITransport

    @LocalServerPort
    lateinit var port: Integer

    @Test
    fun `attaches request and user information to SentryEvents`() {
        val restTemplate = TestRestTemplate().withBasicAuth("user", "password")
        val headers = HttpHeaders()
        headers["X-FORWARDED-FOR"] = listOf("169.128.0.1")
        val entity = HttpEntity<Void>(headers)

        restTemplate.exchange("http://localhost:$port/hello", HttpMethod.GET, entity, Void::class.java)

        await.untilAsserted {
            verify(transport).send(check { event: SentryEvent ->
                assertThat(event.request).isNotNull()
                assertThat(event.request.url).isEqualTo("http://localhost:$port/hello")
                assertThat(event.user).isNotNull()
                assertThat(event.user.username).isEqualTo("user")
                assertThat(event.user.ipAddress).isEqualTo("169.128.0.1")
            })
        }
    }

    @Test
    fun `attaches first ip address if multiple addresses exist in a header`() {
        val restTemplate = TestRestTemplate().withBasicAuth("user", "password")
        val headers = HttpHeaders()
        headers["X-FORWARDED-FOR"] = listOf("169.128.0.1, 192.168.0.1")
        val entity = HttpEntity<Void>(headers)

        restTemplate.exchange("http://localhost:$port/hello", HttpMethod.GET, entity, Void::class.java)

        await.untilAsserted {
            verify(transport).send(check { event: SentryEvent ->
                assertThat(event.user.ipAddress).isEqualTo("169.128.0.1")
            })
        }
    }
}

@SpringBootApplication
open class App

@RestController
class HelloController {

    @GetMapping("/hello")
    fun hello() {
        Sentry.captureMessage("hello")
    }
}

@Configuration
open class SecurityConfiguration(private val hub: IHub) : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity) {
        http
            .addFilterAfter(SentrySecurityFilter(hub), AnonymousAuthenticationFilter::class.java)
            .csrf().disable()
            .authorizeRequests().anyRequest().authenticated()
            .and()
            .httpBasic()
    }

    @Bean
    override fun userDetailsService(): UserDetailsService {
        val encoder: PasswordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder()
        val user: UserDetails = User
            .builder()
            .passwordEncoder { rawPassword -> encoder.encode(rawPassword) }
            .username("user")
            .password("password")
            .roles("USER")
            .build()
        return InMemoryUserDetailsManager(user)
    }
}
