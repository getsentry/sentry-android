package io.sentry.spring.boot;

import com.jakewharton.nopen.annotation.Open;
import io.sentry.core.EventProcessor;
import io.sentry.core.SentryEvent;
import io.sentry.core.protocol.User;
import javax.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/** Attaches user information from the HTTP request to {@link SentryEvent}. */
@Open
public class SentryUserHttpServletRequestProcessor implements EventProcessor {

  @Override
  public SentryEvent process(final @NotNull SentryEvent event, final @Nullable Object hint) {
    final RequestAttributes requestAttributes = RequestContextHolder.currentRequestAttributes();
    if (requestAttributes instanceof ServletRequestAttributes) {
      final HttpServletRequest request =
          ((ServletRequestAttributes) requestAttributes).getRequest();
      event.setUser(toUser(event.getUser(), request));
    }
    return event;
  }

  private static @NotNull User toUser(
      final @Nullable User existingUser, final @NotNull HttpServletRequest request) {
    final User user = existingUser != null ? existingUser : new User();
    user.setIpAddress(toIpAddress(request));
    if (request.getUserPrincipal() != null) {
      user.setUsername(request.getUserPrincipal().getName());
    }
    return user;
  }

  private static @NotNull String toIpAddress(final @NotNull HttpServletRequest request) {
    final String ipAddress = request.getHeader("X-FORWARDED-FOR");
    return ipAddress != null ? ipAddress : request.getRemoteAddr();
  }
}
