package io.sentry.core;

import io.sentry.core.protocol.User;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Removes personal identifiable information from {@link SentryEvent} if {@link
 * SentryOptions#isSendDefaultPii()} is set to false.
 */
@ApiStatus.Internal
final class PiiEventProcessor implements EventProcessor {
  private final @NotNull SentryOptions options;

  PiiEventProcessor(final @NotNull SentryOptions options) {
    this.options = options;
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
    }
    return event;
  }
}
