package io.sentry.core;

import io.sentry.core.util.Objects;

public class MainEventProcessor implements EventProcessor {

  private final SentryOptions options;

  public MainEventProcessor(SentryOptions options) {
    this.options = Objects.requireNonNull(options, "The SentryOptions is required.");
  }

  @Override
  public SentryEvent process(SentryEvent event) {
    return event;
  }
}
