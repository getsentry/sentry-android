package io.sentry.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.context.annotation.Import;

/**
 * Enables Sentry error handling capabilities.
 *
 * <p>- creates bean of type {@link io.sentry.core.SentryOptions} using properties from Spring
 * {@link org.springframework.core.env.Environment}. - registers {@link io.sentry.core.IHub} for
 * sending Sentry events - registers {@link SentryRequestFilter} for attaching request information
 * to Sentry events - registers {@link SentryExceptionResolver} to send Sentry event for any
 * uncaught exception in Spring MVC flow.
 */
@Retention(RetentionPolicy.RUNTIME)
@Import({SentryCoreConfiguration.class, SentryWebConfiguration.class})
@Target(ElementType.TYPE)
public @interface EnableSentry {}
