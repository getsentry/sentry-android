package io.sentry.core;

import io.sentry.core.protocol.Message;
import io.sentry.core.protocol.SentryId;
import io.sentry.core.util.Nullable;

public class SentryClient implements ISentryClient {
  private boolean isEnabled;

  private SentryOptions options;

  public boolean isEnabled() {
    return isEnabled;
  }

  public SentryClient(SentryOptions options) {
    this.options = options;
    this.isEnabled = true;
  }

  public SentryId captureEvent(SentryEvent event, @Nullable Scope scope) {
    ILogger logger = options.getLogger();
    if (logger != null) {
      logger.log(SentryLevel.DEBUG, "Capturing event: %s", event.getEventId());
    }
    return event.getEventId();
  }

  @Override
  public SentryId captureEvent(SentryEvent event) {
    return captureEvent(event, null);
  }

  @Override
  public SentryId captureMessage(String message) {
    return captureMessage(message, null);
  }

  @Override
  public SentryId captureMessage(String message, @Nullable Scope scope) {
    SentryEvent event = new SentryEvent();
    Message sentryMessage = new Message();
    sentryMessage.setFormatted(message);
    return captureEvent(event);
  }

  @Override
  public SentryId captureException(Throwable throwable) {
    return captureException(throwable, null);
  }

  @Override
  public SentryId captureException(Throwable throwable, @Nullable Scope scope) {
    SentryEvent event = new SentryEvent(throwable);
    return captureEvent(event);
  }

  @Override
  public void close(long shutdownMills) {
    ILogger logger = options.getLogger();
    if (logger != null) {
      logger.log(SentryLevel.INFO, "Closing SDK.");
    }
    flush(options.getShutdownTimeout());
    isEnabled = false;
  }

  @Override
  public void flush(long timeoutMills) {
    // TODO: Flush transport
  }
}
