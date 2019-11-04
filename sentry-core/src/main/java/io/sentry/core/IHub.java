package io.sentry.core;

import io.sentry.core.protocol.SentryId;
import org.jetbrains.annotations.Nullable;

public interface IHub {

  boolean isEnabled();

  SentryId captureEvent(SentryEvent event);

  SentryId captureMessage(String message);

  SentryId captureException(Throwable throwable);

  void close();

  void addBreadcrumb(Breadcrumb breadcrumb, @Nullable Hint hint);

  SentryId getLastEventId();

  void pushScope();

  void popScope();

  void withScope(ScopeCallback callback);

  void configureScope(ScopeCallback callback);

  void bindClient(ISentryClient client);

  void flush(long timeoutMills);

  IHub clone();
}
