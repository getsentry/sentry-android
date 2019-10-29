package io.sentry.core;

import io.sentry.core.util.Objects;

public class MainEventProcessor implements EventProcessor {

  private final SentryOptions options;
  private final SentryThreadFactory sentryThreadFactory;
  private final SentryExceptionFactory sentryExceptionFactory;

  MainEventProcessor(
      final SentryOptions options,
      final SentryThreadFactory sentryThreadFactory,
      final SentryExceptionFactory sentryExceptionFactory) {
    this.options = Objects.requireNonNull(options, "The SentryOptions is required.");
    this.sentryThreadFactory =
        Objects.requireNonNull(sentryThreadFactory, "The SentryThreadFactory is required.");
    this.sentryExceptionFactory =
        Objects.requireNonNull(sentryExceptionFactory, "The SentryExceptionFactory is required.");
  }

  @Override
  public SentryEvent process(SentryEvent event) {
    if (event.getThreads() == null) {
      event.setThreads(sentryThreadFactory.getCurrentThreads());
    }

    if (event.getRelease() == null) {
      event.setRelease(options.getRelease());
    }
    if (event.getEnvironment() == null) {
      event.setEnvironment(options.getEnvironment());
    }

    Throwable throwable = event.getThrowable();
    if (throwable != null) {
      event.setExceptions(sentryExceptionFactory.getSentryExceptions(throwable));
    }

    return event;
  }
}
