package io.sentry.core;

import io.sentry.core.exception.SentryExceptionReader;
import io.sentry.core.protocol.Message;
import io.sentry.core.protocol.SentryException;
import io.sentry.core.protocol.SentryStackFrame;
import io.sentry.core.protocol.SentryStackTrace;
import io.sentry.core.util.Objects;
import java.util.ArrayList;
import java.util.List;

public class MainEventProcessor implements EventProcessor {

  private final SentryOptions options;

  public MainEventProcessor(SentryOptions options) {
    this.options = Objects.requireNonNull(options, "The SentryOptions is required.");
  }

  @Override
  public SentryEvent process(SentryEvent event) {
    Throwable throwable = event.getThrowable();
    if (throwable != null) {

      if (event.getMessage() == null) {
        event.setMessage(getMessage(throwable));
      }

      event.setException(SentryExceptionReader.sentryExceptionReader(throwable));
    }

    return event;
  }

  private Message getMessage(Throwable throwable) {
    Message message = new Message();
    message.setFormatted(throwable.getMessage());
    return message;
  }
}
