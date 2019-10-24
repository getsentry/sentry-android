package io.sentry.core.exception;

import io.sentry.core.protocol.Mechanism;
import io.sentry.core.protocol.SentryException;
import io.sentry.core.protocol.SentryStackFrame;
import io.sentry.core.protocol.SentryStackTrace;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class SentryExceptionReader {

  /**
   * Creates a new instance from the given {@code throwable}.
   *
   * @param throwable the {@link Throwable} to build this instance from
   */
  public static List<SentryException> createSentryException(final Throwable throwable) {
    return createSentryException(extractExceptionQueue(throwable));
  }

  /**
   * Creates a new instance from the given {@code exceptions}.
   *
   * @param exceptions a {@link Deque} of {@link SentryException} to build this instance from
   */
  private static List<SentryException> createSentryException(
      final Deque<SentryException> exceptions) {
    return new ArrayList<>(exceptions);
  }

  /**
   * Creates a Sentry exception based on a Java Throwable.
   *
   * <p>The {@code childExceptionStackTrace} parameter is used to define the common frames with the
   * child exception (Exception caused by {@code throwable}).
   *
   * @param throwable Java exception to send to Sentry.
   * @param exceptionMechanism The optional {@link Mechanism} of the {@code throwable}. Or null if
   *     none exist.
   */
  private static SentryException createSentryException(
      Throwable throwable, Mechanism exceptionMechanism) {

    Package exceptionPackage = throwable.getClass().getPackage();
    String fullClassName = throwable.getClass().getName();

    SentryException exception = new SentryException();

    String exceptionMessage = throwable.getMessage();

    String exceptionClassName =
        exceptionPackage != null
            ? fullClassName.replace(exceptionPackage.getName() + ".", "")
            : fullClassName;

    String exceptionPackageName = exceptionPackage != null ? exceptionPackage.getName() : null;

    SentryStackTrace sentryStackTrace = new SentryStackTrace();

    sentryStackTrace.setFrames(getStackFrames(throwable.getStackTrace()));

    exception.setStacktrace(sentryStackTrace);
    exception.setType(exceptionClassName);
    exception.setMechanism(exceptionMechanism);
    exception.setModule(exceptionPackageName);
    exception.setValue(exceptionMessage);

    return exception;
  }

  private static List<SentryStackFrame> getStackFrames(StackTraceElement[] elements) {
    List<SentryStackFrame> sentryStackFrames = new ArrayList<>();

    for(StackTraceElement item : elements) {
      SentryStackFrame sentryStackFrame = new SentryStackFrame();
      sentryStackFrame.setModule(item.getClassName());
      sentryStackFrame.setFunction(item.getMethodName());
      sentryStackFrame.setFilename(item.getFileName());
      sentryStackFrame.setLineno(item.getLineNumber());
      sentryStackFrames.add(sentryStackFrame);
    }
    return sentryStackFrames;
  }

  /**
   * Transforms a {@link Throwable} into a Queue of {@link SentryException}.
   *
   * <p>Exceptions are stored in the queue from the most recent one to the oldest one.
   *
   * @param throwable throwable to transform in a queue of exceptions.
   * @return a queue of exception with StackTrace.
   */
  private static Deque<SentryException> extractExceptionQueue(Throwable throwable) {
    Deque<SentryException> exceptions = new ArrayDeque<>();
    Set<Throwable> circularityDetector = new HashSet<>();
    Mechanism exceptionMechanism;

    // Stack the exceptions to send them in the reverse order
    while (throwable != null && circularityDetector.add(throwable)) {
      if (throwable instanceof ExceptionMechanismThrowable) {
        // this is for ANR I believe
        ExceptionMechanismThrowable exceptionMechanismThrowable =
            (ExceptionMechanismThrowable) throwable;
        exceptionMechanism = exceptionMechanismThrowable.getExceptionMechanism();
        throwable = exceptionMechanismThrowable.getThrowable();
      } else {
        exceptionMechanism = null;
      }

      SentryException exception = createSentryException(throwable, exceptionMechanism);
      exceptions.add(exception);
      throwable = throwable.getCause();
    }

    return exceptions;
  }
}
