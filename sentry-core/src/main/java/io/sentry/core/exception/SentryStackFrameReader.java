package io.sentry.core.exception;

import io.sentry.core.protocol.SentryStackFrame;

import java.util.Map;

public final class SentryStackFrameReader {

  /**
   * Convert an array of {@link StackTraceElement}s to {@link SentryStackFrame}s.
   *
   * @param stackTraceElements Array of {@link StackTraceElement}s to convert.
   * @param cachedFrames Array of cached {@link SentryFrame}s (from the Sentry agent) if available,
   *                     or null.
   * @return Array of {@link SentryStackFrame}s.
   */
  public static SentryStackFrame[] fromStackTraceElements(StackTraceElement[] stackTraceElements,
                                                          SentryFrame[] cachedFrames) {

        /*
        This loop is a bit hairy because it has to deal with cached frames (from FrameCache, set by
        the agent if it is in use).

        - If cachedFrames is null (most commonly: because the agent isn't being used) nothing fancy
          needs to happen.
        - If cachedFrames is not null we need to pair frames from stackTraceElements (the exception
          being captured) to frames found in the FrameCache.

          The issue is that some frameworks/libraries seem to trim stacktraces in some cases, and
          so the array in the FrameCache (set when the original exception was *thrown*) may slightly
          differ (be larger) than the actual exception that is being captured. For this reason we
          need to iterate cachedFrames separately, sometimes advancing 'further' than the equivalent
          index in stackTraceElements (thus skipping elements). In addition, the only information
          we have to "match" frames with is the method name, which is checked for equality between
          the two arrays at each step.

          In the worst case, if something is mangled or weird, we just iterate through the cachedFrames
          immediately (not finding a match) and locals are not set/sent with the event.
         */
    SentryStackFrame[] sentryStackTraceElements = new SentryStackFrame[stackTraceElements.length];
    for (int i = 0, j = 0; i < stackTraceElements.length; i++, j++) {
      StackTraceElement stackTraceElement = stackTraceElements[i];

      Map<String, Object> locals = null;
      if (cachedFrames != null) {
        // step through cachedFrames until we hit a match on the method in the stackTraceElement
        while (j < cachedFrames.length
          && !stackTraceElement.getMethodName().equals(cachedFrames[j].getMethod().getName())) {
          j++;
        }

        // only use cachedFrame locals if we haven't exhausted the array
        if (j < cachedFrames.length) {
          locals = cachedFrames[j].getLocals();
        }
      }

      sentryStackTraceElements[i] = fromStackTraceElement(stackTraceElement, locals);
    }

    return sentryStackTraceElements;
  }

  private static SentryStackFrame fromStackTraceElement(StackTraceElement stackTraceElement,
                                                               Map<String, Object> locals) {
    SentryStackFrame stackFrame = new SentryStackFrame();
    stackFrame.setModule(stackTraceElement.getClassName());
    stackFrame.setFunction(stackTraceElement.getMethodName());
    stackFrame.setFilename(stackTraceElement.getFileName());
    stackFrame.setLineno(stackTraceElement.getLineNumber());
    // TODO: locals?

    return stackFrame;
  }
}
