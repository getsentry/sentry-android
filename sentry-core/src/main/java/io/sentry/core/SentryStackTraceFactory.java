package io.sentry.core;

import io.sentry.core.protocol.SentryStackFrame;
import io.sentry.core.util.Nullable;
import io.sentry.core.util.VisibleForTesting;
import java.util.ArrayList;
import java.util.List;

/** class responsible for converting Java StackTraceElements to SentryStackFrames */
class SentryStackTraceFactory {

  private final String inAppPrefix;
  @VisibleForTesting boolean inAppEnabled = false;

  public SentryStackTraceFactory(@Nullable final String inAppPrefix) {
    this.inAppPrefix = inAppPrefix;

    if (this.inAppPrefix != null && !this.inAppPrefix.isEmpty()) {
      inAppEnabled = true;
    }
  }

  /**
   * convert an Array of Java StackTraceElements to a list of SentryStackFrames
   *
   * @param elements Array of Java StackTraceElements
   * @return list of SentryStackFrames
   */
  List<SentryStackFrame> getStackFrames(@Nullable final StackTraceElement[] elements) {
    List<SentryStackFrame> sentryStackFrames = new ArrayList<>();

    if (elements != null) {
      for (StackTraceElement item : elements) {
        SentryStackFrame sentryStackFrame = new SentryStackFrame();

        // https://docs.sentry.io/development/sdk-dev/features/#in-app-frames
        if (inAppEnabled && item.getClassName().startsWith(inAppPrefix)) {
          sentryStackFrame.setInApp(true);
        } else {
          sentryStackFrame.setInApp(false);
        }

        sentryStackFrame.setModule(item.getClassName());
        sentryStackFrame.setFunction(item.getMethodName());
        sentryStackFrame.setFilename(item.getFileName());
        sentryStackFrame.setLineno(item.getLineNumber());
        sentryStackFrame.setNative(item.isNativeMethod());

        sentryStackFrames.add(sentryStackFrame);
      }
    }

    return sentryStackFrames;
  }
}
