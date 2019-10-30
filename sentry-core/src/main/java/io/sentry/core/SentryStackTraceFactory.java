package io.sentry.core;

import io.sentry.core.protocol.SentryStackFrame;
import io.sentry.core.util.Nullable;
import java.util.ArrayList;
import java.util.List;

/** class responsible for converting Java StackTraceElements to SentryStackFrames */
class SentryStackTraceFactory {

  private final List<String> inAppExcludes;
  private final List<String> inAppIncludes;

  public SentryStackTraceFactory(
      @Nullable final List<String> inAppExcludes, @Nullable List<String> inAppIncludes) {
    this.inAppExcludes = inAppExcludes;
    this.inAppIncludes = inAppIncludes;
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
        sentryStackFrame.setInApp(isInApp(item.getClassName()));
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

  private boolean isInApp(String className) {
    //    if (inAppIncludes != null) {
    //      for (String include : inAppIncludes) {
    //        if (className.startsWith(include)) {
    //          return false;
    //        }
    //      }
    //    }
    if (inAppExcludes != null) {
      for (String exclude : inAppExcludes) {
        if (className.startsWith(exclude)) {
          return true;
        }
      }
    }
    return false;
  }
}
