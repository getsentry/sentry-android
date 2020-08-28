package io.sentry.core;

import io.sentry.core.protocol.Request;
import io.sentry.core.protocol.User;
import io.sentry.core.util.CollectionUtils;
import io.sentry.core.util.Objects;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Removes personal identifiable information from {@link SentryEvent} if {@link
 * SentryOptions#isSendDefaultPii()} is set to false.
 */
final class PiiEventProcessor implements EventProcessor {
  private static final List<String> SENSITIVE_HEADERS =
      Arrays.asList("X-FORWARDED-FOR", "Authorization", "Cookies");

  private final @NotNull SentryOptions options;

  PiiEventProcessor(final @NotNull SentryOptions options) {
    this.options = Objects.requireNonNull(options, "The options object is required");
  }

  @Override
  public SentryEvent process(SentryEvent event, @Nullable Object hint) {
    if (!options.isSendDefaultPii()) {
      final User user = event.getUser();
      if (user != null) {
        user.setUsername(null);
        user.setIpAddress(null);
        user.setEmail(null);
        event.setUser(user);
      }
      final Request request = event.getRequest();
      if (request != null) {
        final Map<String, String> headers = CollectionUtils.shallowCopy(request.getHeaders());
        if (headers != null) {
          for (String sensitiveHeader : SENSITIVE_HEADERS) {
            headers.remove(sensitiveHeader);
          }
          request.setHeaders(headers);
        }
        if (request.getCookies() != null) {
          request.setCookies(null);
        }
      }
    }
    return event;
  }
}
