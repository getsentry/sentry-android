package io.sentry.core;

public interface Integration {
  void register(HubWrapper hub, SentryOptions options);
}
