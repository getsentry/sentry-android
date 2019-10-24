package io.sentry.core;

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

//      List<SentryException> exceptions = new ArrayList<>();
//      exceptions.add(extractExceptionQueue(event.getThrowable()));
//      event.setException(exceptions);
    }

    return event;
  }

  private Message getMessage(Throwable throwable) {
    Message message = new Message();
    message.setFormatted(throwable.getMessage());
    return message;
  }

  private SentryException extractExceptionQueue(Throwable throwable) {
    //    throwable.

    SentryException sentryException = new SentryException();
    SentryStackTrace sentryStackTrace = new SentryStackTrace();
    List<SentryStackFrame> frames = new ArrayList<>();

    for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
      SentryStackFrame stackFrame = new SentryStackFrame();
      stackFrame.setRawFunction(stackTraceElement.getMethodName());
      stackFrame.setModule(stackTraceElement.getClassName());
      stackFrame.setFilename(stackTraceElement.getFileName());
      stackFrame.setLineno(stackTraceElement.getLineNumber());

      frames.add(stackFrame);
    }
    sentryStackTrace.setFrames(frames);
    sentryException.setStacktrace(sentryStackTrace);

    return sentryException;
  }
}
