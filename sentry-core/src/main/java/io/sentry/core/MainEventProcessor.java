package io.sentry.core;

import io.sentry.core.util.ApplyScopeUtils;
import io.sentry.core.util.Objects;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public final class MainEventProcessor implements EventProcessor {

  private final SentryOptions options;
  private final SentryThreadFactory sentryThreadFactory;
  private final SentryExceptionFactory sentryExceptionFactory;

  MainEventProcessor(final SentryOptions options) {
    this.options = Objects.requireNonNull(options, "The SentryOptions is required.");

    SentryStackTraceFactory sentryStackTraceFactory =
        new SentryStackTraceFactory(options.getInAppExcludes(), options.getInAppIncludes());

    sentryExceptionFactory = new SentryExceptionFactory(sentryStackTraceFactory);
    sentryThreadFactory =
        new SentryThreadFactory(sentryStackTraceFactory, this.options.isAttachStacktrace());
  }

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
  public SentryEvent process(SentryEvent event, @Nullable Object hint) {
    if (event.getPlatform() == null) {
      // this actually means JVM related.
      event.setPlatform("java");
    }

    Throwable throwable = event.getThrowable();
    if (throwable != null) {
      event.setExceptions(sentryExceptionFactory.getSentryExceptions(throwable));
    }

    if (ApplyScopeUtils.shouldApplyScopeData(hint)) {
      processNonCachedEvent(event);
    } else {
      options
          .getLogger()
          .log(
              SentryLevel.DEBUG,
              "Event was cached so not applying data relevant to the current app execution/version: %s",
              event.getEventId());
    }

    return event;
  }

  private void processNonCachedEvent(SentryEvent event) {
    if (event.getRelease() == null) {
      event.setRelease(options.getRelease());
    }
    if (event.getEnvironment() == null) {
      event.setEnvironment(options.getEnvironment());
    }
    if (event.getServerName() == null) {
      event.setServerName(options.getServerName());
    }
    if (event.getDist() == null) {
      event.setDist(options.getDist());
    }

    if (event.getThreads() == null && options.isAttachThreads()) {
      event.setThreads(sentryThreadFactory.getCurrentThreads());
    }
  }
}
