package io.sentry.core.transport;

import io.sentry.core.SentryEvent;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class NoOpHttpTransport implements ITransport {
  private static final NoOpHttpTransport instance = new NoOpHttpTransport();

  public static NoOpHttpTransport getInstance() {
    return instance;
  }

  private NoOpHttpTransport() {}

  @Override
  public TransportResult send(SentryEvent event) {
    return TransportResult.error(-1, -1);
  }

  @Override
  public void close() {}
}
