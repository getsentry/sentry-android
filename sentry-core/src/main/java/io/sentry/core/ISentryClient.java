package io.sentry.core;

import io.sentry.core.protocol.SentryId;
import io.sentry.core.util.Nullable;

public interface ISentryClient {
  boolean isEnabled();

  SentryId captureEvent(SentryEvent event);

  SentryId captureEvent(SentryEvent event, @Nullable Scope scope);

  SentryId captureMessage(String message);

  SentryId captureMessage(String message, @Nullable Scope scope);

  SentryId captureException(Throwable throwable);

  SentryId captureException(Throwable throwable, @Nullable Scope scope);

  void close(long shutdownMills);

  void flush(long timeoutMills);
}
