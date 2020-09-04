package io.sentry.spring;

import com.jakewharton.nopen.annotation.Open;
import io.sentry.core.IHub;
import io.sentry.core.SentryOptions;
import io.sentry.core.util.Objects;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;

/** Pushes new {@link io.sentry.core.Scope} on each incoming HTTP request. */
@Open
public class SentryRequestFilter extends OncePerRequestFilter implements Ordered {
  private final @NotNull IHub hub;
  private final @NotNull SentryOptions options;

  public SentryRequestFilter(final @NotNull IHub hub, final @NotNull SentryOptions options) {
    this.hub = Objects.requireNonNull(hub, "hub is required");
    this.options = Objects.requireNonNull(options, "options are required");
  }

  @Override
  protected void doFilterInternal(
      final @NotNull HttpServletRequest request,
      final @NotNull HttpServletResponse response,
      final @NotNull FilterChain filterChain)
      throws ServletException, IOException {
    hub.pushScope();
    // clears breadcrumbs that may have been added during application startup through one of the
    // logging integrations.
    hub.clearBreadcrumbs();
    hub.addBreadcrumb(createRequestBreadcrumb(request));

    hub.configureScope(
        scope -> {
          scope.addEventProcessor(new SentryRequestHttpServletRequestProcessor(request, options));
        });
    filterChain.doFilter(request, response);
  }

  private @NotNull String createRequestBreadcrumb(final @NotNull HttpServletRequest request) {
    return "Starting to serve request " + request.getMethod() + " " + request.getRequestURI();
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }
}
