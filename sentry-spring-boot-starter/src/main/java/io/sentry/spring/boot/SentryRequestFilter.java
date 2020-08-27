package io.sentry.spring.boot;

import com.jakewharton.nopen.annotation.Open;
import io.sentry.core.IHub;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.filter.OncePerRequestFilter;

/** Pushes new {@link io.sentry.core.Scope} on each incoming HTTP request. */
@Open
public class SentryRequestFilter extends OncePerRequestFilter {
  private final @NotNull IHub hub;

  public SentryRequestFilter(final @NotNull IHub hub) {
    this.hub = hub;
  }

  @Override
  protected void doFilterInternal(
      final @NotNull HttpServletRequest request,
      final @NotNull HttpServletResponse response,
      final @NotNull FilterChain filterChain)
      throws ServletException, IOException {
    hub.pushScope();

    hub.configureScope(
        scope -> {
          scope.addEventProcessor(new SentryRequestHttpServletRequestProcessor(request));
        });
    filterChain.doFilter(request, response);
  }
}
