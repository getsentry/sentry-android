package io.sentry.core.transport;

import io.sentry.core.SentryEvent;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

public interface Connection {
  void send(SentryEvent event, @Nullable Object hint) throws IOException;
  default void send(SentryEvent event) throws IOException {
    send(event, null);
  }

  void close() throws IOException;
}
